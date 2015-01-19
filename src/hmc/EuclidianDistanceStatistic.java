package hmc;

import beast.inference.hamilton.HamiltonUpdate;
import beast.inference.model.Statistic;
import beast.xml.ObjectElement;
import beast.xml.Parseable;
import beast.xml.SimpleXMLObjectParser;
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
        return hamiltonUpdate.getDistance();
    }

    public static final XMLObjectParser<EuclidianDistanceStatistic> PARSER = new SimpleXMLObjectParser<>(EuclidianDistanceStatistic.class, "");

}
