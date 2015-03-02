package hmc;

import beast.inference.hamilton.HamiltonUpdate;
import beast.inference.model.Statistic;
import beast.xml.ObjectElement;
import beast.xml.Parseable;
import beast.xml.SimpleXMLObjectParser;
import beast.xml.SimpleXMLObjectParser.ParserCreationException;
import beast.xml.XMLObjectParser;

/**
 * @author Arman Bilge
 */
public class EuclidianDistanceStatistic extends Statistic.Abstract {

    private final HamiltonUpdate hamiltonUpdate;

    @Parseable
    public EuclidianDistanceStatistic(@ObjectElement(name = "hamilton") HamiltonUpdate hu) {
        hamiltonUpdate = hu;
    }

    @Override
    public int getDimension() {
        return 1;
    }

    @Override
    public double getStatisticValue(int dim) {
        return 0.0;//hamiltonUpdate.getDistance();
    }

    public static final XMLObjectParser<EuclidianDistanceStatistic> PARSER;

    static {
        try {
            PARSER = new SimpleXMLObjectParser(EuclidianDistanceStatistic.class);
        } catch (ParserCreationException e) {
            throw new RuntimeException(e);
        }
    }

}
