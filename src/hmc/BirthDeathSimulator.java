package hmc;

import beast.evolution.tree.SimpleNode;
import beast.evolution.tree.SimpleTree;
import beast.evolution.tree.Tree;
import beast.evolution.util.Taxa;
import beast.inference.model.Parameter;
import beast.math.MathUtils;
import beast.xml.AbstractXMLObjectParser;
import beast.xml.ElementRule;
import beast.xml.XMLObject;
import beast.xml.XMLParseException;
import beast.xml.XMLSyntaxRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Arman Bilge
 */
public class BirthDeathSimulator extends AbstractXMLObjectParser<SimpleTree> {

    private static final String BIRTH_RATE = "birthRate";
    private static final String DEATH_RATE = "deathRate";

    @Override
    public SimpleTree parseXMLObject(XMLObject xo) throws XMLParseException {
        final Taxa taxa = xo.getChild(Taxa.class);
        final Parameter birthRate = xo.getChild(BIRTH_RATE).getChild(Parameter.class);
        final Parameter deathRate = xo.getChild(DEATH_RATE).getChild(Parameter.class);
        final int n = taxa.getTaxonCount();
        final double lambda = birthRate.getParameterValue(0);
        final double mu = deathRate.getParameterValue(0);
        final double origin = sampleOrigin(n, lambda, mu);
        final double[] times = new double[n-1];
        Arrays.setAll(times, i -> sampleSpeciation(origin, lambda, mu));
        Arrays.sort(times);
        final Set<SimpleNode> nodes = new HashSet<>(n);
        for (int i = 0; i < n; ++i) {
            final SimpleNode node = new SimpleNode();
            node.setTaxon(taxa.getTaxon(i));
            node.setHeight(0);
            nodes.add(node);
        }
        for (int i = 0; i < n - 1; ++i) {
            final SimpleNode p = new SimpleNode();
            p.setHeight(times[i]);
            p.addChild(removeRandomElement(nodes));
            p.addChild(removeRandomElement(nodes));
            nodes.add(p);
        }
        final SimpleTree tree = new SimpleTree(removeRandomElement(nodes));
        System.out.println(Tree.Utils.newick(tree));
        return tree;
    }

    @Override
    public XMLSyntaxRule[] getSyntaxRules() {
        return new XMLSyntaxRule[] {
            new ElementRule(Taxa.class),
            new ElementRule(BIRTH_RATE, Parameter.class),
            new ElementRule(DEATH_RATE, Parameter.class)
        };
    }

    @Override
    public String getParserDescription() {
        return "Simulates a birth-death tree.";
    }

    @Override
    public Class<SimpleTree> getReturnType() {
        return SimpleTree.class;
    }

    @Override
    public String getParserName() {
        return "birthDeathSimulator";
    }

    private static double sampleOrigin(final int n, final double lambda, final double mu) {
        final double x = MathUtils.nextDouble();
        return Math.log((mu * Math.pow(x, 1.0 / n) - lambda) / (lambda * (Math.pow(x, 1.0 / n) - 1))) / (lambda - mu);
    }

    private static double sampleSpeciation(final double t, final double lambda, final double mu) {
        final double x = MathUtils.nextDouble();
        return Math.log((lambda - mu * Math.exp((-lambda + mu) * t) - mu * (1 - Math.exp((-lambda + mu) * t)) * x) / (lambda - mu * Math.exp((-lambda + mu) * t) - lambda * (1 - Math.exp((-lambda + mu) * t)) * x)) / (lambda - mu);
    }

    private static <E> E removeRandomElement(final Collection<E> c) {
        final int r = MathUtils.nextInt(c.size());
        int i = 0;
        for (final Iterator<E> iter = c.iterator(); iter.hasNext();) {
            final E e = iter.next();
            if (r == i++) {
                iter.remove();
                return e;
            }
        }
        return null;
    }

}
