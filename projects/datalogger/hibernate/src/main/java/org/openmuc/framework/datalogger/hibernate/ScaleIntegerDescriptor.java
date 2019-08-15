package org.openmuc.framework.datalogger.hibernate;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public class ScaleIntegerDescriptor extends AbstractTypeDescriptor<Long> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4711264359790671417L;
	
	private static final String FACTOR = "Factor";
	// Factor should be greater or equal 1000 and of type Integer. Factor 1000 converts
	// milliseconds in seconds. 
	private static final String DEFAULT_FACTOR = "1000";

	public static final JavaTypeDescriptor<Long> INSTANCE = 
    	      new ScaleIntegerDescriptor();
	
	private double factor;
    	 
	protected ScaleIntegerDescriptor() {
		super(Long.class, ImmutableMutabilityPlan.INSTANCE);
		int intFactor = Integer.valueOf(System.getProperty(FACTOR, DEFAULT_FACTOR));
		if (intFactor < 1000) intFactor = 1000;
		factor = Double.valueOf(intFactor);
	}

	@Override
	public String toString(Long value) {
		return value.toString();
	}

	@Override
	public Long fromString(String string) {
		return Long.valueOf(string);
	}

	@Override
	public <X> X unwrap(Long value, Class<X> type, WrapperOptions options) {
		if (value == null) return null;
		Integer intValue = (int) Math.round(((long)value)/factor);
		value.intValue();
		return (X) intValue;
	}

	@Override
	public <X> Long wrap(X value, WrapperOptions options) {
		if (value == null) return null;
		if (Integer.class.isInstance(value)) {
			Long newValue = new Long((Integer)value);
			newValue = (long) Math.round(newValue * factor);
			return newValue;
		}
		throw unknownWrap(value.getClass());
	}

}
