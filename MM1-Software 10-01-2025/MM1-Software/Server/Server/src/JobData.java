package Server;

/**
 * Stores the measurement values for a Job.
 * 
 * @author Kirschner
 *
 */
public class JobData {

    private long calcTime;
    private long packetLength;
    private long responseTime;
    private long cpuTime;
    public void setCalcTime (long calcTime) {
        this.calcTime = calcTime;
    }
    
    public void setPacketLength (long packetLength) {
        this.packetLength = packetLength;
    }
    
    public void setResponseTime (long responseTime) {
        this.responseTime = responseTime;
    }
    public void setCpuTime(long cpuTime){this.cpuTime=cpuTime;}
    
    public long getCalcTime () {
        return calcTime;
    }
    
    public long getPacketLength () {
        return packetLength;
    }
    
    public long getResponseTime () {
        return responseTime;
    }
    public long getCpuTime(){return cpuTime;}
}
