package Main;

public class Rate {

    private final RateType type;

    /**
     * the fee you pay upfront
     */
    private final double baseFee;

    /**
     * included time you get to park for paying the base fee
     */
    private final int baseHours;

    /**
     * succeeding rate you need to pay after staying longer that baseHours
     */
    private final double extraFee;

    //constructor
    public Rate (RateType type, int baseHours, double baseFee, double extraFee) {
        if (type == null || baseFee < 0 || baseHours < 0 || extraFee < 0) {
            throw new IllegalArgumentException("Invalid rate values");
        }

        this.type = type;
        this.baseHours = baseHours;
        this.baseFee = baseFee;
        this.extraFee = extraFee;
    }

    //getters
    public RateType getType() {
        return type;
    }
    public double getBaseFee() {
        return baseFee;
    }
    public int getBaseHours() {
        return baseHours;
    }
    public double getExtraFee() {
        return extraFee;
    }

    double computeFee(double hours){
        if(hours < 0){
            throw new IllegalArgumentException("Hours cannot be negative");
        }

        if (type == RateType.FIXED){
           return baseFee;
        }
        if (type == RateType.HOURLY){
            if(hours <= baseHours){
                return baseFee;
            }else{
                return baseFee + (hours - baseHours) * extraFee;
            }
        }
        return baseFee;
    }


}
