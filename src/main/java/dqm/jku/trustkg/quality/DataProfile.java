package dqm.jku.trustkg.quality;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.cyberborean.rdfbeans.annotations.RDF;
import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFContainer;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;
import org.cyberborean.rdfbeans.annotations.RDFSubject;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;

import dqm.jku.trustkg.dsd.elements.Attribute;
import dqm.jku.trustkg.dsd.elements.DSDElement;
import dqm.jku.trustkg.dsd.records.Record;
import dqm.jku.trustkg.dsd.records.RecordList;
import dqm.jku.trustkg.quality.profilingmetrics.MetricTitle;
import dqm.jku.trustkg.quality.profilingmetrics.ProfileMetric;
import dqm.jku.trustkg.quality.profilingmetrics.singlecolumn.valuelength.*;
import dqm.jku.trustkg.quality.profilingmetrics.singlecolumn.cardinality.Cardinality;
import dqm.jku.trustkg.quality.profilingmetrics.singlecolumn.cardinality.NullValues;
import dqm.jku.trustkg.quality.profilingmetrics.singlecolumn.cardinality.Size;
import dqm.jku.trustkg.quality.profilingmetrics.singlecolumn.cardinality.Uniqueness;
import dqm.jku.trustkg.quality.profilingmetrics.singlecolumn.dependency.KeyCandidate;
import dqm.jku.trustkg.quality.profilingmetrics.singlecolumn.distribution.*;
import dqm.jku.trustkg.util.Miscellaneous.DBType;
import dqm.jku.trustkg.util.numericvals.NumberComparator;

import static dqm.jku.trustkg.quality.profilingmetrics.MetricTitle.*;

@RDFNamespaces({ "foaf = http://xmlns.com/foaf/0.1/", })
@RDFBean("foaf:DataProfile")
public class DataProfile {
  private List<ProfileMetric> metrics = new ArrayList<>();
  private DSDElement elem;
  private String uri;

  //TODO: patterns, user-generated and generic => occurencecheck in style of Null-Value principle (Boolean expressions?)
  public DataProfile() {

  }

  public DataProfile(RecordList rs, DSDElement d) throws NoSuchMethodException {
    this.elem = d;
    this.uri = elem.getURI() + "/profile";
    createReferenceDataProfile();
    calculateReferenceDataProfile(rs);
  }

  /**
   * calculates an initial data profile based on the values inserted in the
   * reference profile
   * 
   * @param rs the set of records used for calculation
   * @param d  the dsd element to be annotated (currently only Attribute for
   *           single column metrics)
   * @throws NoSuchMethodException 
   */
  private void calculateReferenceDataProfile(RecordList rs) throws NoSuchMethodException {
    if (elem instanceof Attribute) calculateSingleColumn(rs);
  }

  /**
   * Gets the URI
   * 
   * @return uri
   */
  @RDFSubject
  public String getURI() {
    return uri;
  }

  /**
   * Sets the URI (security threat but used by rdfbeans)
   * 
   * @param uri
   */
  public void setURI(String uri) {
    this.uri = uri;
  }

  /**
   * Helper method for calculating the single column values for the profile
   * 
   * @param rs the recordset for measuring
   * @throws NoSuchMethodException 
   */
  private void calculateSingleColumn(RecordList rs) throws NoSuchMethodException {
    List<Number> l = createValueList(rs);
    for (ProfileMetric p : metrics) {
      if (p.getTitle().equals(nullVal)) p.calculation(rs, p.getValue());
      else p.calculationNumeric(l, p.getValue());
    }

  }

  /**
   * Helper method for creating a list of numeric values
   * 
   * @param rs the record set for measuring
   * @return list of numeric values of the records
   */
  private List<Number> createValueList(RecordList rs) {
    List<Number> list = new ArrayList<Number>();
    Attribute a = (Attribute) elem;
    for (Record r : rs) {
      Number field = null;
      Class<?> clazz = a.getDataType();
      if (String.class.isAssignableFrom(clazz) && r.getField(a) != null) field = ((String) r.getField(a)).length();
      else if(a.getConcept().getDatasource().getDBType().equals(DBType.CSV)) {
    	  field = (Number) r.getField(a);
      } else if(a.getConcept().getDatasource().getDBType().equals(DBType.MYSQL)) {
    	  if(Number.class.isAssignableFrom(clazz)) {
    		  field = (Number) r.getField(a);
    	  } 
      } 
      if (field != null) {
    	  list.add(field);
      }
    }
    list.sort(new NumberComparator());
    list.size();
    return list;
  }

  /**
   * creates a reference data profile on which calculations can be made
   */
  private void createReferenceDataProfile() {
    if (elem instanceof Attribute) {
    	Attribute a = (Attribute) elem;
    	Class<?> clazz = a.getDataType();
    	if(String.class.isAssignableFrom(clazz) || Number.class.isAssignableFrom(clazz)) {
    	      ProfileMetric size = new Size(this);
    	      metrics.add(size);
    	      ProfileMetric min = new Minimum(this);
    	      metrics.add(min);
    	      ProfileMetric max = new Maximum(this);
    	      metrics.add(max);
    	      ProfileMetric avg = new Average(this);
    	      metrics.add(avg);
    	      ProfileMetric med = new Median(this);
    	      metrics.add(med);
    	      ProfileMetric card = new Cardinality(this);
    	      metrics.add(card);
    	      ProfileMetric uniq = new Uniqueness(this);
    	      metrics.add(uniq);
    	      ProfileMetric nullVal = new NullValues(this);
    	      metrics.add(nullVal);
    	      ProfileMetric hist = new Histogram(this);
    	      metrics.add(hist);
    	      ProfileMetric digits = new Digits(this);
    	      metrics.add(digits);
    	      ProfileMetric isCK = new KeyCandidate(this);
    	      metrics.add(isCK);
    	      ProfileMetric decimals = new Decimals(this);
    	      metrics.add(decimals);
    	} else {
    		System.err.println("Attribute '" + a.getLabel() + "' has data type '" + a.getDataTypeString() + "', which is currently not handled. ");
    	}
    }
  }

  /**
   * Method for printing out the data profile
   */
  public void printProfile() {
    System.out.println("Data Profile:");
    if (metrics.stream().anyMatch(p -> p.getValueClass().equals(String.class))) System.out.println("Strings use String length for value length metrics!");
    SortedSet<ProfileMetric> metricSorted = new TreeSet<>();
    metricSorted.addAll(metrics);
    for (ProfileMetric p : metricSorted) {
      System.out.println(p.toString());
    }
    System.out.println();
  }

  /**
   * Method for getting the set of metrics
   * 
   * @return set of metrics
   */
  @RDF("foaf:includes")
  @RDFContainer
  public List<ProfileMetric> getMetrics() {
    return metrics;
  }

  /**
   * Sets the metrics (security threat but used by rdfbeans)
   * 
   * @param metrics the metrics to set
   */
  public void setMetrics(List<ProfileMetric> metrics) {
    this.metrics = metrics;
  }
  
  /**
   * Method for getting a specific ProfileMetric with its corresponding label.
   * @param label the label to compare with the profile metrics
   * @return ProfileMetric if found, null otherwise
   */
  public ProfileMetric getMetric(MetricTitle title) {
    for (ProfileMetric m : metrics) {
      if (m.getTitle().equals(title)) return m;
    }
    return null;
  }

  /**
   * Sets the dsd element (security threat but used by rdfbeans)
   * 
   * @param elem the elem to set
   */
  public void setElem(DSDElement elem) {
    this.elem = elem;
  }

  /**
   * Gets the reference dsd element, used for calculation
   * 
   * @return the reference element
   */
  @RDF("foaf:annotatedTo")
  public DSDElement getElem() {
    return elem;
  }

  public Point createMeasuringPoint(Builder measure) {
    SortedSet<ProfileMetric> metricSorted = new TreeSet<>();
    metricSorted.addAll(metrics);
    for (ProfileMetric p : metricSorted) {
      if (!p.getTitle().equals(hist)) addMeasuringValue(p, measure);
    }
    return measure.build();
  }

  private void addMeasuringValue(ProfileMetric p, Builder measure) {
    if (p.getValue() == null) return;
    if (p.getValueClass().equals(Long.class)) measure.addField(p.getLabel(), (long) p.getValue());
    else if (p.getValueClass().equals(Double.class)) measure.addField(p.getLabel(), (double) p.getValue());
    else measure.addField(p.getLabel(), (int) p.getValue());
  }

}
