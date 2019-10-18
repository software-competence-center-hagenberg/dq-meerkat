package dqm.jku.trustkg.quality.profilingmetrics.singlecolumn.cardinality;

import java.util.List;

import dqm.jku.trustkg.dsd.elements.Attribute;
import dqm.jku.trustkg.dsd.records.Record;
import dqm.jku.trustkg.dsd.records.RecordSet;
import dqm.jku.trustkg.quality.DataProfile;
import dqm.jku.trustkg.quality.profilingmetrics.ProfileMetric;

public class NullValues extends ProfileMetric {
  private static final String name = "Null Values";
  
  public NullValues() {
    
  }
  
  public NullValues(DataProfile d) {
    super(name, d);
  }

  @Override
  public void calculation(RecordSet rs, Object oldVal) {
    Attribute a = (Attribute) super.getRefElem();
    long nullVals = 0;
    for (Record r : rs) {
      if (r.getField(a) == null) nullVals++;
    }
    this.setValue(nullVals);
    this.setValueClass(a.getDataType());

  }

  @Override
  public void calculationNumeric(List<Number> list, Object oldVal) throws NoSuchMethodException {
    throw new NoSuchMethodException("Calculation has to be performed with Records!");
  }

  @Override
  public void update(RecordSet rs) {
    calculation(rs, super.getValue());
  }

  @Override
  protected String getValueString() {
    if (getValue() == null) return "\tnull";
    else return "\t" + getValue().toString() + " (" + ((long)getValue() / (int)super.getRefProf().getMetric("Size").getValue()) + "%)";
  }

}
