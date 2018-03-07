package org.openmuc.framework.app.household.grid;

import org.openmuc.framework.app.household.HouseholdApp;
import org.openmuc.framework.data.Flag;
import org.openmuc.framework.data.Record;
import org.openmuc.framework.dataaccess.RecordListener;

public class PowerListener implements RecordListener {

    /**
     * Interface used to notify the {@link HouseholdApp} 
     * implementation about changed power values
     */
    public interface PowerCallbacks {
        public void onPowerReceived(PowerType type, Record record);
    }

    /**
     * The Listeners' current callback object, which is notified of changed power values
     */
    private final PowerCallbacks callbacks;

    private final PowerType type;

    public PowerListener(PowerCallbacks callbacks, PowerType type) {
        this.callbacks = callbacks;
        this.type = type;
    }

    public PowerType getType() {
        return type;
    }

    @Override
    public void newRecord(Record record) {
        if (record.getFlag() == Flag.VALID && record.getValue() != null) {
            callbacks.onPowerReceived(type, record);
        }
    }
}
