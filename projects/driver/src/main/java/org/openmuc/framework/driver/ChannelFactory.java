package org.openmuc.framework.driver;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.openmuc.framework.config.Address;
import org.openmuc.framework.config.ArgumentSyntaxException;
import org.openmuc.framework.config.Settings;

public interface ChannelFactory {

    @Documented
    @Inherited
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, })
    public static @interface Factory {

        Class<? extends ChannelContainer> channel();

        Class<? extends ChannelScanner> scanner() default ChannelScanner.class;

    }

    ChannelContainer newChannel(Address address, Settings settings) throws ArgumentSyntaxException;

}
