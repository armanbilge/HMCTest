
package hmc;

import beast.evolution.alignment.Alignment;
import beast.evolution.alignment.SimpleAlignment;
import beast.evolution.datatype.Nucleotides;
import beast.evolution.io.NewickImporter;
import beast.evolution.sequence.Sequence;
import beast.evolution.tree.NodeRef;
import beast.evolution.tree.Tree;
import beast.evomodel.branchratemodel.BranchRateModel;
import beast.evomodel.branchratemodel.DefaultBranchRateModel;
import beast.evomodel.sitemodel.GammaSiteModel;
import beast.evomodel.sitemodel.SiteModel;
import beast.evomodel.substmodel.FrequencyModel;
import beast.evomodel.substmodel.HKY;
import beast.inference.model.Parameter;
import beast.math.MathUtils;
import beast.xml.AbstractXMLObjectParser;
import beast.xml.AttributeRule;
import beast.xml.ElementRule;
import beast.xml.XMLObject;
import beast.xml.XMLObjectParser;
import beast.xml.XMLParseException;
import beast.xml.XMLSyntaxRule;

/** Class for performing random sequence generation for a given site model.
 * Sequences for the leave nodes in the tree are returned as an alignment.
 *
 * @author remco@cs.waikato.ac.nz
 *
 */

public class SequenceSimulator {
	
	private static final boolean DEBUG = false;
	
	/** nr of samples to generate **/
	protected int m_sequenceLength;
	/** tree used for generating samples **/
    protected Tree m_tree;
	/** site model used for generating samples **/
    protected SiteModel m_siteModel;
	/** branch rate model used for generating samples **/
    protected BranchRateModel m_branchRateModel;
    /** nr of categories in site model **/
    int m_categoryCount;
    /** nr of states in site model **/
    int m_stateCount;

	static boolean has_ancestralSequence = false;
	protected Sequence ancestralSequence;
    
    /**
     * an array used to transfer transition probabilities
     */
    protected double[][] m_probabilities;

    /**
     * Constructor
     * @param tree
     * @param siteModel
     * @param branchRateModel
     * @param sequenceLength: nr of sites to generate
     */
    SequenceSimulator(Tree tree, SiteModel siteModel, BranchRateModel branchRateModel, int sequenceLength) {
    	m_tree = tree;
    	m_siteModel = siteModel;
    	m_branchRateModel = branchRateModel;
    	m_sequenceLength = sequenceLength;
    	m_stateCount = m_siteModel.getFrequencyModel().getDataType().getStateCount();
        m_categoryCount = m_siteModel.getCategoryCount();
        m_probabilities = new double[m_categoryCount][m_stateCount * m_stateCount];
    } // c'tor

    /**
     * Convert integer representation of sequence into a Sequence
     * @param seq integer representation of the sequence
     * @param node used to determine taxon for sequence
     * @return Sequence
     */
	Sequence intArray2Sequence(int [] seq, NodeRef node) {
    	StringBuilder sSeq = new StringBuilder();
    	for (int i  = 0; i < m_sequenceLength; i++) {
    		sSeq.append(m_siteModel.getFrequencyModel().getDataType().getCode(seq[i]));
    	}
		return new Sequence(m_tree.getNodeTaxon(node), sSeq.toString());
    } // intArray2Sequence

	void setAncestralSequence(Sequence seq) {
		ancestralSequence = seq;
		has_ancestralSequence = true;
	}
	
	int[] sequence2intArray(Sequence seq) {

		if(seq.getLength() != m_sequenceLength) {

			throw new RuntimeException("Ancestral sequence length has "
					+ seq.getLength() + " characters " + "expecting "
					+ m_sequenceLength + " characters");

		}
		
		int array[] = new int[m_sequenceLength];
		for (int i = 0; i < m_sequenceLength; i++) {
			array[i] = m_siteModel.getFrequencyModel().getDataType().getState(
					seq.getChar(i));
		}
		return array;
	}//END: sequence2intArray
	
	/**
	 * perform the actual sequence generation
	 * @return alignment containing randomly generated sequences for the nodes in the
	 * leaves of the tree
	 */
	public Alignment simulate() {
		NodeRef root = m_tree.getRoot();

		double[] categoryProbs = m_siteModel.getCategoryProportions();
		int[] category = new int[m_sequenceLength];
		for (int i = 0; i < m_sequenceLength; i++) {
			category[i] = MathUtils.randomChoicePDF(categoryProbs);
		}

		int[] seq = new int[m_sequenceLength];

		if (has_ancestralSequence) {

			seq = sequence2intArray(ancestralSequence);
			
		} else {

			FrequencyModel frequencyModel = m_siteModel.getFrequencyModel();
			for (int i = 0; i < m_sequenceLength; i++) {
				seq[i] = MathUtils.randomChoicePDF(frequencyModel
						.getFrequencies());
			}

		}

		if (DEBUG) {
			synchronized (this) {
				System.out.println();
				System.out.println("root Sequence:");
//				Utils.printArray(seq);
			}
		}
		
		SimpleAlignment alignment = new SimpleAlignment();
//		alignment.setReportCountStatistics(false);
		alignment.setDataType(m_siteModel.getFrequencyModel().getDataType());

		traverse(root, seq, category, alignment);

		return alignment;
	} // END: simulate

	/**
	 * recursively walk through the tree top down, and add sequence to alignment whenever
	 * a leave node is reached.
	 * @param node reference to the current node, for which we visit all children
	 * @param parentSequence randomly generated sequence of the parent node
	 * @param category array of categories for each of the sites
	 * @param alignment
	 */
	void traverse(NodeRef node, int [] parentSequence, int [] category, SimpleAlignment alignment) {
		
		if (DEBUG) {
			System.out.println();
			    System.out.println("I'm at: " + node.toString());
				System.out.println();
		}
		
		for (int iChild = 0; iChild < m_tree.getChildCount(node); iChild++) {
			
			NodeRef child = m_tree.getChild(node, iChild);
			
            for (int i = 0; i < m_categoryCount; i++) {
            	getTransitionProbabilities(m_tree, child, i, m_probabilities[i]);
            }

			if (DEBUG) {
			     	System.out.println("Going to child " + iChild + ": " + child.toString());
					System.out.println("Child finite transition probs matrix:");
//					Utils.print2DArray(m_probabilities, 4);
					System.out.println();
			}// END: if DEBUG
            
        	int [] seq = new int[m_sequenceLength];
    		double [] cProb = new double[m_stateCount];
    		
        	for (int i  = 0; i < m_sequenceLength; i++) {
        		
        		//System.out.println("seqChar " + parentSequence[i]);
        		
        		System.arraycopy(m_probabilities[category[i]], parentSequence[i] * m_stateCount, cProb, 0, m_stateCount);
        		
				if (DEBUG) {
						System.out.println("site:" + i);
						System.out.println("site probs:");
//						Utils.printArray(cProb);
				}// END: if DEBUG

				seq[i] = MathUtils.randomChoicePDF(cProb);

        	}

			if (DEBUG) {
//        	    seq = new int[]{1, 3, 2, 3, 0, 1, 0, 1, 0, 2, 2, 0, 1, 3, 3, 3, 0, 1, 2, 1, 3, 1, 1, 1, 1, 3, 0, 0, 3, 2, 3, 2, 3, 2, 1, 2, 1, 3, 2, 3, 3, 0, 2, 2, 3, 2, 3, 2, 3, 1, 2, 0, 2, 1, 3, 2, 3, 1, 1, 1, 1, 0, 2, 3, 1, 0, 2, 1, 2, 1, 3, 0, 0, 0, 0, 0, 2, 0, 2, 3, 1, 0, 1, 3, 0, 2, 1, 2, 1, 3, 0, 0, 3, 2, 2, 0, 1, 0, 0, 3  };
				System.out.println("Simulated sequence:");
//				Utils.printArray(seq);
			}
        	
            if (m_tree.getChildCount(child) == 0) {
            	
            	if (DEBUG) {
            		System.out.println("Simulated sequence (translated):");
                    System.out.println(intArray2Sequence(seq, child).getSequenceString());
            	}
        
            	alignment.addSequence(intArray2Sequence(seq, child));
            }
            
			traverse(m_tree.getChild(node, iChild), seq, category, alignment);
		}//END: child nodes loop
		
	} // traverse

    void getTransitionProbabilities(Tree tree, NodeRef node, int rateCategory, double[] probs) {

        NodeRef parent = tree.getParent(node);

        final double branchRate = m_branchRateModel.getBranchRate(tree, node);

        // Get the operational time of the branch
        final double branchTime = branchRate * (tree.getNodeHeight(parent) - tree.getNodeHeight(node));

        if (branchTime < 0.0) {
            throw new RuntimeException("Negative branch length: " + branchTime);
        }

        double branchLength = m_siteModel.getRateForCategory(rateCategory) * branchTime;

        m_siteModel.getSubstitutionModel().getTransitionProbabilities(branchLength, probs);
    } // getTransitionProbabilities

    /** helper method **/
    public static void printUsageAndExit() {
		System.err.println("Usage: java " + SequenceSimulator.class.getName() + " <nr of instantiations>");
		System.err.println("where <nr of instantiations> is the number of instantiations to be replciated");
		System.exit(0);
	} // printUsageAndExit

    /* standard xml parser stuff follows */
    public static final String SEQUENCE_SIMULATOR = "sequenceSimulator";
    public static final String SITE_MODEL = SiteModel.SITE_MODEL;
    public static final String TREE = "tree";
//    public static final String BRANCH_RATE_MODEL = "branchRateModel";
    public static final String REPLICATIONS = "replications";

    public static XMLObjectParser PARSER = new AbstractXMLObjectParser() {

        public String getParserName() {
            return SEQUENCE_SIMULATOR;
        }

        public Object parseXMLObject(XMLObject xo) throws XMLParseException {

        	int nReplications = xo.getIntegerAttribute(REPLICATIONS);

            Tree tree = (Tree) xo.getChild(Tree.class);
            SiteModel siteModel = (SiteModel) xo.getChild(SiteModel.class);
            BranchRateModel rateModel = (BranchRateModel)xo.getChild(BranchRateModel.class);
            Sequence ancestralSequence = (Sequence)xo.getChild(Sequence.class);
            
            if (rateModel == null)
            	rateModel = new DefaultBranchRateModel();

            SequenceSimulator s = new SequenceSimulator(tree, siteModel, rateModel, nReplications);

            if(ancestralSequence != null) {
                s.setAncestralSequence(ancestralSequence);
            }
            
            return s.simulate();
        }

        //************************************************************************
        // AbstractXMLObjectParser implementation
        //************************************************************************

        public String getParserDescription() {
            return "A SequenceSimulator that generates random sequences for a given tree, sitemodel and branch rate model";
        }

        public Class getReturnType() {
            return Alignment.class;
        }

        public XMLSyntaxRule[] getSyntaxRules() {
            return rules;
        }

        private XMLSyntaxRule[] rules = new XMLSyntaxRule[]{
                new ElementRule(Tree.class),
                new ElementRule(SiteModel.class),
                new ElementRule(BranchRateModel.class, true),
                new ElementRule(Sequence.class, true),
                AttributeRule.newIntegerRule(REPLICATIONS)
        };
    };

    /** generate simple site model, for testing purposes **/
    static SiteModel getDefaultSiteModel() {
		Parameter kappa = new Parameter.Default(1, 2);
        Parameter freqs = new Parameter.Default(new double[]{0.25, 0.25, 0.25, 0.25});
        FrequencyModel f = new FrequencyModel(Nucleotides.INSTANCE, freqs);
        HKY hky = new HKY(kappa, f);
        return new GammaSiteModel(hky);
	} // getDefaultSiteModel

    public static void main(String [] args) {
    	
    	try {
    	
    		int nReplications = 10;

    		// create tree
    		NewickImporter importer = new NewickImporter("((A:1.0,B:1.0)AB:1.0,(C:1.0,D:1.0)CD:1.0)ABCD;");
    		Tree tree =  importer.importTree(null);
    		
    		// create site model
    		SiteModel siteModel = getDefaultSiteModel();
    		
    		// create branch rate model
    		BranchRateModel branchRateModel = new DefaultBranchRateModel();

    		// feed to sequence simulator and generate leaves
    		SequenceSimulator treeSimulator = new SequenceSimulator(tree, siteModel, branchRateModel, nReplications);

    		Sequence ancestralSequence = new Sequence();
    		ancestralSequence.appendSequenceString("TCAGGTCAAG");
    		treeSimulator.setAncestralSequence(ancestralSequence);
    		
    		System.out.println(treeSimulator.simulate().toString());

		} catch (Exception e) {
			e.printStackTrace();
		}//END: try-catch block
		
	} // END: main
    
//    public static void main(String [] args) {
//    	try {
//    		if (args.length == 0) {
//    			printUsageAndExit();
//    		}
//    		int nReplications = Integer.parseInt(args[0]);
//
//    		// create tree
//    		NewickImporter importer = new NewickImporter("((A:1.0,B:1.0)AB:1.0,(C:1.0,D:1.0)CD:1.0)ABCD;");
//    		Tree tree =  importer.importTree(null);
//    		// create site model
//    		SiteModel siteModel = getDefaultSiteModel();
//    		// create branch rate model
//    		BranchRateModel branchRateModel = new DefaultBranchRateModel();
//
//    		// feed to sequence simulator and generate leaves
//    		SequenceSimulator treeSimulator = new SequenceSimulator(tree, siteModel, branchRateModel, nReplications);
//    		System.err.println(treeSimulator.simulate().toString());
//    		treeSimulator.simulate();
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    	}
//	} // main

} // class SequenceSimulator
