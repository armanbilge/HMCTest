package hmc;

import beast.evolution.tree.SimpleTree;
import beast.math.MathUtils;
import beast.xml.AbstractXMLObjectParser;
import beast.xml.XMLObject;
import beast.xml.XMLParseException;
import beast.xml.XMLSyntaxRule;

/**
 * @author Arman Bilge
 */
public class BirthDeathSimulator extends AbstractXMLObjectParser<SimpleTree> {

    @Override
    public SimpleTree parseXMLObject(XMLObject xo) throws XMLParseException {
        return null;
    }

    @Override
    public XMLSyntaxRule[] getSyntaxRules() {
        return new XMLSyntaxRule[] {

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
        return Math.log(-(lambda * Math.exp((lambda - mu) * t) - mu + x * mu * (Math.exp((lambda - mu) * t) - 1)) / (- lambda * Math.exp((lambda - mu) * t) + mu - lambda * x * (Math.exp((lambda - mu) * t) - 1))) / (lambda - mu);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 50; ++i) {
//            System.out.println(sampleOrigin(16, 2, 1));
            System.out.println(sampleSpeciation(3.0, 2, 1));
        }
    }

}
