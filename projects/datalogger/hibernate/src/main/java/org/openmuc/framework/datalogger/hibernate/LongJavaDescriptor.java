package org.openmuc.framework.datalogger.hibernate;

import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.AbstractTypeDescriptor;
import org.hibernate.type.descriptor.java.ImmutableMutabilityPlan;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public class LongJavaDescriptor extends AbstractTypeDescriptor<Long> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -4711264359790671417L;
	
	public static final JavaTypeDescriptor<Long> INSTANCE = 
    	      new LongJavaDescriptor();
    	 
	protected LongJavaDescriptor() {
		super(Long.class, ImmutableMutabilityPlan.INSTANCE);
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
		Integer intValue = (int) Math.round(((Long)value)/1000.0);
		return (X) intValue;
	}

	@Override
	public <X> Long wrap(X value, WrapperOptions options) {
		if (value == null) return null;
		if (Integer.class.isInstance(value)) {
			Long newValue = new Long((Integer)value);
			newValue *= 1000;
			return newValue;
		}
		throw unknownWrap(value.getClass());
	}

}
