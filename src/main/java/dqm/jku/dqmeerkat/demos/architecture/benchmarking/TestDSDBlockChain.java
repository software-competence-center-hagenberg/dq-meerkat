package dqm.jku.dqmeerkat.demos.architecture.benchmarking;

import java.io.IOException;
import java.util.ArrayList;

import dqm.jku.dqmeerkat.blockchain.blocks.DSDBlock;
import dqm.jku.dqmeerkat.blockchain.standardchain.BlockChain;
import dqm.jku.dqmeerkat.connectors.ConnectorCSV;
import dqm.jku.dqmeerkat.connectors.DSConnector;
import dqm.jku.dqmeerkat.dsd.elements.Attribute;
import dqm.jku.dqmeerkat.dsd.elements.Concept;
import dqm.jku.dqmeerkat.dsd.elements.DSDElement;
import dqm.jku.dqmeerkat.dsd.elements.Datasource;

/**
 * Test class for testing of blockchains containing DSD elements
 * 
 * @author optimusseptim
 *
 */
public class TestDSDBlockChain {

  public static void main(String args[]) {
    int difficultySetting = 6;

    // Create Connection to CSV Connector
    DSConnector conn = new ConnectorCSV("src/main/java/dqm/jku/dqmeerkat/resources/Telematic Device Report - Device Voltage.csv", ",", "\n", "Device Voltage", true);

    BlockChain bC1 = new BlockChain();
    BlockChain bC2 = new BlockChain(difficultySetting, "test"); // testing with difficulty higher by one

    try {
      Datasource ds = conn.loadSchema();
      ArrayList<DSDElement> elements = new ArrayList<>();
      for (Concept c : ds.getConcepts()) {
        elements.add(c);
        for (Attribute a : c.getAttributes()) {
          elements.add(a);
        }
      }
      long time1 = measureCreatingTime(bC1, elements);
      long time2 = measureCreatingTime(bC2, elements);

      long result = time2 - time1;
      if (result < 0) System.out.println(String.format("Mesuring with difficulty %d was faster by %d ms than measuring with standard difficulty", difficultySetting, Math.abs(result)));
      else if (result > 0) System.out.println(String.format("Mesuring with standard difficulty was faster by %d ms than measuring with difficulty %d", Math.abs(result), difficultySetting));
      else if (result == 0) System.out.println("Both methods are equally fast!");

    } catch (IOException e) {
      System.err.println("Could not load schema!");
    }

  }

  /**
   * Makes a measurement for creation with a Blockchain
   * 
   * @param bC       the blockchain to be tested
   * @param elements the elements to be added
   * @return creation time
   */
  private static long measureCreatingTime(BlockChain bC, ArrayList<DSDElement> elements) {
    System.out.println("Starting with Blockchain creation test");
    long sTime = System.currentTimeMillis();
    for (DSDElement e : elements) {
      bC.addBlock(new DSDBlock(bC.getId(), bC.getPreviousHash(), e));
      System.out.println("Added block with element: " + e.getURI());
    }
    long eTime = System.currentTimeMillis();
    long result = eTime - sTime;
    System.out.println(String.format("Test ended with testsize of %d blocks and a time of %d ms", elements.size(), result));
    System.out.println();
    return result;
  }
}
