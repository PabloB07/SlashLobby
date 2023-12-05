package me.rodrigo.slashlobby.lib;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Map;

public class Parser {
    private final Path Path;
    private Map<String, Object> AllData = null;
    public Parser(final Path Path) throws FileNotFoundException {
        this.Path = Path;
        final Yaml Parser = new Yaml();
        this.AllData = Parser.load(new FileInputStream(this.Path.toString()));
    }

    @Deprecated
    public static int RETURN_NULL_ON_EXCEPTION = 4902;
    @Deprecated
    public static int RETURN_0_ON_EXCEPTION = 9152;

    public String AsString(final String Path, int... Modifier) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (String) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            String FinalData = null;
            for ( int i = 0; i < Parts.length; i++ ) {
                final String Part = Parts[i];
                if ( i + 1 ==  Parts.length ) {
                    FinalData = (String) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {
            throw new RuntimeException(E);
        }
    }

    public Map<String, Object> RawGet(String Path) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (Map<String, Object>) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            Map<String, Object> FinalData = null;
            for ( int i = 0; i < Parts.length; i++ ) {
                final String Part = Parts[i];
                if ( i + 1 ==  Parts.length ) {
                    FinalData = (Map<String, Object>) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {
            throw new RuntimeException(E);
        }
    }

    public Object AsObject(String Path) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (Map<String, Object>) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            Object FinalData = null;
            for ( int i = 0; i < Parts.length; i++ ) {
                final String Part = Parts[i];
                if ( i + 1 ==  Parts.length ) {
                    FinalData = (Object) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {
            throw new RuntimeException(E);
        }
    }

    public ArrayList AsArrayList(final String Path, int... Modifier) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (ArrayList) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            ArrayList FinalData = null;
            for (int i = 0; i < Parts.length; i++) {
                final String Part = Parts[i];
                if (i + 1 == Parts.length) {
                    FinalData = (ArrayList) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {

            throw new RuntimeException(E);
        }
    }

    public boolean AsBoolean(final String Path, int... Modifier) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (boolean) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            boolean FinalData = false;
            for ( int i = 0; i < Parts.length; i++ ) {
                final String Part = Parts[i];
                if ( i + 1 ==  Parts.length ) {
                    FinalData = (boolean) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {
            return false;
        }
    }

    public ArrayList<String> AsStringList(final String Path,int... Modifier) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (ArrayList<String>) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            ArrayList<String> FinalData = null;
            for (int i = 0; i < Parts.length; i++) {
                final String Part = Parts[i];
                if (i + 1 == Parts.length) {
                    FinalData = (ArrayList<String>) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {

            throw new RuntimeException(E);
        }
    }

    public ArrayList<Integer> AsIntegerList(final String Path, int... Modifier) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (ArrayList<Integer>) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            ArrayList<Integer> FinalData = null;
            for (int i = 0; i < Parts.length; i++) {
                final String Part = Parts[i];
                if (i + 1 == Parts.length) {
                    FinalData = (ArrayList<Integer>) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {

            throw new RuntimeException(E);
        }
    }

    public ArrayList<Double> AsDoubleList(final String Path, int... Modifier) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (ArrayList<Double>) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            ArrayList<Double> FinalData = null;
            for (int i = 0; i < Parts.length; i++) {
                final String Part = Parts[i];
                if (i + 1 == Parts.length) {
                    FinalData = (ArrayList<Double>) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {

            throw new RuntimeException(E);
        }
    }

    public ArrayList<Long> AsLongList(final String Path, int... Modifier) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (ArrayList<Long>) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            ArrayList<Long> FinalData = null;
            for (int i = 0; i < Parts.length; i++) {
                final String Part = Parts[i];
                if (i + 1 == Parts.length) {
                    FinalData = (ArrayList<Long>) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {

            throw new RuntimeException(E);
        }
    }

    public int AsInt(final String Path, int... Modifier) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (int) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            int FinalData = 0;
            for (int i = 0; i < Parts.length; i++) {
                final String Part = Parts[i];
                if (i + 1 == Parts.length) {
                    FinalData = (int) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {
            throw new RuntimeException(E);
        }
    }

    public long AsLong(final String Path, int... Modifier) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (long) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            long FinalData = 0;
            for (int i = 0; i < Parts.length; i++) {
                final String Part = Parts[i];
                if (i + 1 == Parts.length) {
                    FinalData = (long) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {
            throw new RuntimeException(E);
        }
    }

    public double AsDouble(final String Path, int... Modifier) {
        try {
            final String[] Parts = Path.split("\\.");
            if (Parts.length == 1) return (double) AllData.get(Parts[0]);
            Map<String, Object> LastData = AllData;
            double FinalData = 0;
            for (int i = 0; i < Parts.length; i++) {
                final String Part = Parts[i];
                if (i + 1 == Parts.length) {
                    FinalData = (double) LastData.get(Part);
                } else {
                    LastData = (Map<String, Object>) LastData.get(Part);
                }
            }
            return FinalData;
        } catch (Exception E) {
            throw new RuntimeException(E);
        }
    }
}