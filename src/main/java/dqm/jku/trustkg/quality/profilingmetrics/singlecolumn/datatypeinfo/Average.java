package dqm.jku.trustkg.quality.profilingmetrics.singlecolumn.datatypeinfo;

import java.util.List;

import org.cyberborean.rdfbeans.annotations.RDFBean;
import org.cyberborean.rdfbeans.annotations.RDFNamespaces;

import dqm.jku.trustkg.dsd.elements.Attribute;
import dqm.jku.trustkg.dsd.records.Record;
import dqm.jku.trustkg.dsd.records.RecordList;
import dqm.jku.trustkg.quality.DataProfile;
import dqm.jku.trustkg.quality.profilingmetrics.ProfileMetric;
import dqm.jku.trustkg.util.numericvals.NumberComparator;

import static dqm.jku.trustkg.quality.profilingmetrics.MetricTitle.*;

@RDFNamespaces({ "foaf = http://xmlns.com/foaf/0.1/", })
@RDFBean("foaf:Average")
public class Average extends ProfileMetric {
  public Average() {

  }

  public Average(DataProfile d) {
    super(avg, d);
  }

  @Override
  public void calculation(RecordList rs, Object oldVal) {
    Object val = null;
    if (oldVal == null) val = getBasicInstance();
    else val = oldVal;
    for (Record r : rs) {
      Object field = r.getField((Attribute) super.getRefElem());
      val = addValue(val, field);
    }
    val = performAveraging(val);
    this.setValue(val);
    this.setValueClass(((Attribute) super.getRefElem()).getDataType());
  }

  /**
   * Method for getting the average value of the objects
   * 
   * @param sum the sum of values
   * @return the average value
   */
  private Object performAveraging(Object sum) {
    Attribute a = (Attribute) super.getRefElem();
    if (a.getDataType().equals(Long.class)) return (long) sum / (int) super.getRefProf().getMetric(size).getValue();
    else if (a.getDataType().equals(Double.class)) return (double) sum / (int) super.getRefProf().getMetric(size).getValue();
    return (int) sum / (int) super.getRefProf().getMetric(size).getValue();
  }

  /**
   * Creates a basic instance used as a reference (in this case zero as a number)
   * 
   * @return the reference value
   */
  private Object getBasicInstance() {
    Attribute a = (Attribute) super.getRefElem();
    if (a.getDataType().equals(Long.class)) return Long.valueOf(0);
    else if (a.getDataType().equals(Double.class)) return Double.valueOf(0);
    else return Integer.valueOf(0);
  }

  /**
   * Adds a value to the sum of values
   * 
   * @param current the current sum of values
   * @param toAdd   the value to be added
   * @return the new sum of values
   */
  private Object addValue(Object current, Object toAdd) {
    if (toAdd == null) return current;
    Attribute a = (Attribute) super.getRefElem();
    if (a.getDataType().equals(Long.class)) return (long) current + ((Number) toAdd).longValue();
    else if (a.getDataType().equals(Double.class)) return (double) current + ((Number) toAdd).doubleValue();
    else if (toAdd.getClass().equals(String.class)) return (int) current + ((String) toAdd).length();
    else return (int) current + (int) toAdd;
  }

  @Override
  public void update(RecordList rs) {
    calculation(rs, getOriginalSum());
  }

  /**
   * Restores the original sum of the averaging value
   * 
   * @return the sum before weighting with the amount of values
   */
  private Object getOriginalSum() {
    Attribute a = (Attribute) super.getRefElem();
    if (a.getDataType().equals(Long.class)) return ((Number) super.getValue()).longValue() * (int) super.getRefProf().getMetric(size).getValue();
    else if (a.getDataType().equals(Double.class)) return ((Number) super.getValue()).doubleValue() * (int) super.getRefProf().getMetric(size).getValue();
    else return ((int) super.getValue()) * (int) super.getRefProf().getMetric(size).getValue();
  }

  @Override
  public void calculationNumeric(List<Number> list, Object oldVal) {
    if (list == null || list.isEmpty()) {
      if (oldVal != null) return;
      else this.setValue(null);
    } else {
      list.sort(new NumberComparator());
      Object sum = null;
      if (oldVal == null) sum = getBasicInstance();
      else sum = getOriginalSum();
      for (Number n : list) {
        sum = addValue(sum, n);
      }
      sum = performAveraging(sum);
      this.setValue(sum);

    }
    Attribute a = (Attribute) super.getRefElem();
    this.setValueClass(a.getDataType());
  }

  @Override
  protected String getValueString() {
    return super.getSimpleValueString();
  }

}