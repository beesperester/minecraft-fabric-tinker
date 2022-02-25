package beesperester.callofthewild.utilities;

public class StringUtilities {
    public static String getRegexFromGlob(String glob) {
        StringBuilder out = new StringBuilder("^");

        for (int i = 0; i < glob.length(); ++i) {
            final char c = glob.charAt(i);
            switch (c) {
                case '*':
                    out.append(".*");
                    break;
                case '?':
                    out.append('.');
                    break;
                case '.':
                    out.append("\\.");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                default:
                    out.append(c);
            }
        }
        out.append('$');

        return out.toString();
    }
}
