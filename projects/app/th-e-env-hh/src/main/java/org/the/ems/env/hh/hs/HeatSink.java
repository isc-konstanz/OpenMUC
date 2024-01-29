package org.the.ems.env.hh.hs;

public interface HeatSink {

	void set(double power);

	double getSetpoint();

	double getPower();

	double getPowerMax();

}
