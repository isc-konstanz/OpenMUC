package org.the.ems.env.hh.hs;

import org.the.ems.env.Controllable;

public interface HeatSink extends Controllable {

	default void setPowerSetpoint(double power) {
		set(power/getPowerMax() * 100.);
	}

	double getPowerMax();

	double getPower();

}
