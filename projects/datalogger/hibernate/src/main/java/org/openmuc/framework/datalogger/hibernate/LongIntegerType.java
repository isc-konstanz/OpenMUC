package org.openmuc.framework.datalogger.hibernate;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.IntegerTypeDescriptor;

public class LongIntegerType extends AbstractSingleColumnStandardBasicType<Long> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7171959951207336491L;
	
	public static final LongIntegerType INSTANCE = new LongIntegerType();
    
    public LongIntegerType() {
		super(IntegerTypeDescriptor.INSTANCE, LongJavaDescriptor.INSTANCE);
	}

	@Override
	public String getName() {
		return "LongInteger";
	}

}
