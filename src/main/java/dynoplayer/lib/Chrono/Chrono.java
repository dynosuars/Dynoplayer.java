package dynoplayer.lib.Chrono;


/**
 * Helper class for time related things
 */
public class Chrono {

    // Helper enum
    private static enum Symbol{
        SS,
        MM,
        HH
    };

    /**
     * Casts time based on seconds
     * @param time
     * @return
     * @throws Exception
     */
    public static long time_cast(String time) throws Exception{
        String[] vals = time.split(":");
        int[] mapped = new int[vals.length];

        for(int i=0; i<vals.length; i++){
            mapped[i] = Integer.parseInt(vals[i]);
        }
        
        switch (vals.length) {
            case 1:
                return Seconds.parse("%SS", mapped);
            case 2:
                return Seconds.parse("%MM%SS", mapped);
            case 3:
                return Seconds.parse("%HH%MM%SS", mapped);
            default:
                throw new Exception("Chronolib::INVALID TIME error pls fix frfrfrfr");
        }
    }


    public static class Seconds{
        /**
         * Parse and returns it into Second
         * @param fmt
         * @param con
         * @return
         */
        public static long parse(String fmt, int... con) throws Exception {
            String[] fmts = fmt.split("%");
            if(fmts.length -1 != con.length)
                throw new Exception("Yo chat ur format ain't even match with the input gng. What are we doing??????");

            long total = 0;

            for(int i = 1; i < fmts.length; i++){
                Symbol ts = Symbol.valueOf(fmts[i]);

                total += Math.pow(60, ts.ordinal()) * con[con.length - ts.ordinal() - 1];
            }

            return total;

            
        }   
    }


}
