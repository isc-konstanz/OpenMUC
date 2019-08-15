package org.openmuc.framework.datalogger.hibernate;

import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.descriptor.sql.IntegerTypeDescriptor;

public class ScaleIntegerType extends AbstractSingleColumnStandardBasicType<Long> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7171959951207336491L;
	
	public static final ScaleIntegerType INSTANCE = new ScaleIntegerType();
    
    public ScaleIntegerType() {
		super(IntegerTypeDescriptor.INSTANCE, ScaleIntegerDescriptor.INSTANCE);
	}

	@Override
	public String getName() {
		return "ScaleInteger";
	}

}
