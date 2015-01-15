package hmc;

import beast.evolution.tree.NodeRef;
import beast.evolution.tree.SimpleTree;
import beast.evolution.tree.Tree;
import beast.math.MathUtils;
import beast.xml.AbstractXMLObjectParser;
import beast.xml.ElementRule;
import beast.xml.XMLObject;
import beast.xml.XMLParseException;
import beast.xml.XMLSyntaxRule;

/**
 * @author Arman Bilge
 */
public class RandomizeHeights extends AbstractXMLObjectParser<SimpleTree> {

    @Override
    public SimpleTree parseXMLObject(final XMLObject xo) throws XMLParseException {
        final SimpleTree tree = new SimpleTree(xo.getChild(Tree.class));
        for (int i = 0; i < tree.getInternalNodeCount(); ++i) {
            final NodeRef n = tree.getInternalNode(i);
            final double lower = Math.max(tree.getNodeHeight(tree.getChild(n, 0)), tree.getNodeHeight(tree.getChild(n, 1)));
            final double upper = tree.isRoot(n) ? 2 * tree.getNodeHeight(n) : tree.getNodeHeight(tree.getParent(n));
            tree.setNodeHeight(n, MathUtils.nextDouble() * (upper - lower) + lower);
        }
        return tree;
    }

    @Override
    public XMLSyntaxRule[] getSyntaxRules() {
        return new XMLSyntaxRule[] {new ElementRule(Tree.class)};
    }

    @Override
    public String getParserDescription() {
        return "Provides a new, random tree that retains topology.";
    }

    @Override
    public Class<SimpleTree> getReturnType() {
        return SimpleTree.class;
    }

    @Override
    public String getParserName() {
        return "randomizeHeights";
    }

}
