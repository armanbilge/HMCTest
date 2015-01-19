package hmc;

import beast.app.plugin.Plugin;
import beast.xml.XMLObjectParser;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Arman Bilge
 */
public class TestHMC implements Plugin {

    @Override
    public Set<XMLObjectParser> getParsers() {
        final Set<XMLObjectParser> parsers = new HashSet<>();
        parsers.add(new RandomizeHeights());
        parsers.add(new BirthDeathSimulator());
        parsers.add(SequenceSimulator.PARSER);
        parsers.add(EuclidianDistanceStatistic.PARSER);
        return parsers;
    }
}
