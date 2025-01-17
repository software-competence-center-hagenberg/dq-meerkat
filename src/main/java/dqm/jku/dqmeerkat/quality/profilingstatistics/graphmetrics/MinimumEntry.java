package dqm.jku.dqmeerkat.quality.profilingstatistics.graphmetrics;

import dqm.jku.dqmeerkat.dsd.elements.Attribute;
import dqm.jku.dqmeerkat.dsd.elements.Concept;
import dqm.jku.dqmeerkat.dsd.records.Record;
import dqm.jku.dqmeerkat.dsd.records.RecordList;
import dqm.jku.dqmeerkat.quality.DataProfile;
import dqm.jku.dqmeerkat.quality.profilingstatistics.ProfileStatistic;
import dqm.jku.dqmeerkat.util.AttributeSet;

import static dqm.jku.dqmeerkat.quality.profilingstatistics.StatisticTitle.*;
import static dqm.jku.dqmeerkat.quality.profilingstatistics.StatisticCategory.*;

import java.util.List;
import java.util.regex.Pattern;

public class MinimumEntry extends ProfileStatistic {

    private Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public MinimumEntry() {

    }

    public MinimumEntry(DataProfile d) {
        super(minimum, graphCat, d);
    }

    @Override
    public void calculation(RecordList rs, Object oldVal) {
        Concept c = (Concept) super.getRefElem();
        Object val = null;

        Attribute a = getAttribute(rs, c);

        boolean isNumeric = checkNumeric(rs, a);
        Class clazz = null;

        if (isNumeric) {
            clazz = getNumericClass(rs, a);

        } else {
            clazz = String.class;
        }

        if(oldVal == null) {
            val = getBasicInstance(clazz);
        } else {
            val = oldVal;
        }

        for(Record r : rs) {
            Object field = r.getField(a);
            val = getMinimum(val, field, isNumeric, clazz);
        }

        this.setValue(val);
        this.setNumericVal(((Number) val).doubleValue());
        this.setValueClass(clazz);
    }

    @Override
    public void calculationNumeric(List<Number> list, Object oldVal) throws NoSuchMethodException {

    }

    @Override
    public void update(RecordList rs) {

    }

    @Override
    protected String getValueString() {
        return super.getSimpleValueString();
    }

    @Override
    public boolean checkConformance(ProfileStatistic m, double threshold) {
        return false;
    }

    private Object getBasicInstance(Class clazz) {
        if (clazz.equals(Long.class)) {
            return Long.valueOf(Long.MAX_VALUE);
        }
        else if (clazz.equals(Double.class)) {
            return Double.valueOf(Double.MAX_VALUE);
        }
        else {
            return Integer.MAX_VALUE;
        }
    }

    private Object getMinimum(Object current, Object toComp, boolean isNumeric, Class clazz) {
        if (toComp == null) {
            return current;
        }

        if (clazz.equals(Long.class)) {

            return Long.min((long) current, ((Number) Long.parseLong(toComp.toString())).longValue());
        }
        else if (clazz.equals(Double.class)) {
            return Double.min((double) current, ((Number) Double.parseDouble(toComp.toString())).doubleValue());
        }
        else if (clazz.equals(String.class)) {
            return Integer.min((int) current, ((String) toComp).length());
        }
        else {
            return Integer.min((int) current, ((Number) Integer.parseInt(toComp.toString())).intValue());
        }
    }

    private Attribute getAttribute(RecordList rl, Concept c) {

        AttributeSet as = c.getAttributes();

        Record r = rl.toList().get(0);

        for(Attribute a : as) {
            Object val = r.getField(a);

            if(val != null) {
                return a;
            }
        }
        return null;
    }

    private boolean checkNumeric(RecordList rl, Attribute a) {

        boolean isNumeric = true;

        for(Record r : rl) {
            String field = r.getField(a).toString();

            if(field != null && !pattern.matcher(field).matches()) {
                isNumeric = false;
            }
        }

        return isNumeric;
    }

    private Class getNumericClass(RecordList rl, Attribute a) {

        Class clazz = Integer.class;

        for(Record r : rl) {
            String field = r.getField(a).toString();

            try {
                int num = Integer.parseInt(field);
            } catch (NumberFormatException e) {
                try {
                    double num = Double.parseDouble(field);
                    clazz = Double.class;
                } catch (NumberFormatException ex) {

                }
            }
        }
        return clazz;
    }
}
