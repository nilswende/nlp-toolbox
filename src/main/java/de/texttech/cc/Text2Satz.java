package de.texttech.cc;


import de.texttech.cc.converter.ConvertPdf;
import de.texttech.cc.converter.Html2Text;
//import de.texttech.ce.BonusTags;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.*;
import java.text.BreakIterator;
import java.util.*;

// Referenced classes of package de.texttech.cc:
//            CharacterIteratorStream

public class Text2Satz
{

    private HashSet abbreviations;
    private Locale language;
    private String database;
    private String htmlConverter;
    private String dbConnectionName;
    private String workingDir;
    static int success = 0;
    private boolean keep_newline;
    private boolean wrapLongLines;
    private boolean rawMode;
 //   private BonusTags discoveryConverter;
    static final int MAX_TAG_LEN = 25;
    public boolean verbose;
    protected String htmlConverterCmdArray[];
    protected int htmlConverterArgPos;

    public static void usage()
        throws IllegalArgumentException
    {
        System.err.println("Segments text to sentences and saves them into one file.");
        System.err.println("Usage: text2satz -L {de|en|fr} -d datenbank [-h htmlConverter] [-a abkuerzungsda" +
"tei] [-n] [textfiles|-f filefile]"
);
        System.err.println("          -n: keep line breaks as sentence borders");
        System.err.println("          -h: htmlConverter; either 'internal' or");
        System.err.println("              a quoted call like 'lynx -dump -nolist -nopause -width=255'");
        System.err.println("          -f: read names of infiles from given file");
        System.err.println("              If neither -f nor a list of files are given,");
        System.err.println("              text2satz reads file standard input for file names.");
        System.err.println("          -p: working dir where to create temp and out files");
        System.err.println("          -w: wrap long sentences after next space after 220th column");
        System.err.println("          -r: raw mode: don't do any separation, but delete heading numbers+tabs");
        System.err.println("          -V: print version information");
        System.err.println("          -v: generate more verbose output");
        throw new IllegalArgumentException("Text2Satz failed");
    }

    public Text2Satz()
    {
        abbreviations = null;
        language = Locale.GERMANY;
        database = "db";
        htmlConverter = null;
        dbConnectionName = null;
        workingDir = null;
        keep_newline = false;
        wrapLongLines = false;
        rawMode = false;
       // discoveryConverter = null;
        verbose = false;
        htmlConverterCmdArray = null;
        htmlConverterArgPos = 0;
        success = 0;
    }

    public int getNrOfTextsProcessed()
    {
        return success;
    }

    public void setLanguage(Locale locale)
    {
        language = locale;
    }

    public void setDatabase(String s)
    {
        database = s;
    }

    public void setHtmlConverter(String s)
    {
        htmlConverter = s;
        StringTokenizer stringtokenizer = new StringTokenizer(htmlConverter);
        int i = stringtokenizer.countTokens();
        htmlConverterCmdArray = new String[i + 1];
        int j;
        for(j = 0; j < i && stringtokenizer.hasMoreTokens(); j++)
        {
            htmlConverterCmdArray[j] = stringtokenizer.nextToken();
        }

        htmlConverterArgPos = j;
    }

    public void setWorkingDir(String s)
    {
        workingDir = s;
    }

    public void setKeepNewline(boolean flag)
    {
        keep_newline = flag;
    }

    public void setWrapLongLines(boolean flag)
    {
        wrapLongLines = flag;
    }

    public void setDbConnectionName(String s)
    {
        dbConnectionName = s;
     
    }

    public void setRawMode(boolean flag)
    {
        rawMode = flag;
    }

    public void readAbbrevs(String s)
    {
        abbreviations = new HashSet();
        try
        {
            StringBuffer stringbuffer = new StringBuffer("");
            FileInputStream fileinputstream = new FileInputStream(s);
            char c;
            while((c = (char)fileinputstream.read()) != '\uFFFF') 
            {
                if(c == '\n' || c == ' ')
                {
                    abbreviations.add(stringbuffer.toString());
                    stringbuffer.delete(0, stringbuffer.length());
                } else
                {
                    stringbuffer.append(c);
                }
            }
            abbreviations.remove("");
        }
        catch(Exception exception)
        {
            System.err.println("Problems while reading abbreviations:" + exception.getMessage());
        }
    }

    private int writeRestOfLine(DataOutputStream dataoutputstream, BufferedInputStream bufferedinputstream)
        throws IOException
    {
        int j = 0;
        int i;
        for(i = bufferedinputstream.read(); i != 10 && i != 13 && i != -1; i = bufferedinputstream.read())
        {
            j++;
            dataoutputstream.write((char)i);
        }

        if(i != -1)
        {
            dataoutputstream.write((char)i);
        }
        return j;
    }

    private String taggedHeader(String s)
    {
    //    return new String(" <quelle><name>" + database + Integer.toString(success + 1) + "</name><name_lang>" + s + "</name_lang></quelle>\n");
        return new String("");

    
    }

    private DataOutputStream getOutStream(String s)
        throws IOException
    {
        if(success > 0)
        {
            return new DataOutputStream(new FileOutputStream(s, true));
        } else
        {
            return new DataOutputStream(new FileOutputStream(s));
        }
    }

    private int printInRawMode(String s, File file, String s1)
    {
        BufferedInputStream bufferedinputstream = null;
        DataOutputStream dataoutputstream = null;
        try
        {
            bufferedinputstream = new BufferedInputStream(new FileInputStream(file));
            dataoutputstream = getOutStream(s1);
        }
        catch(IOException ioexception)
        {
            System.err.println("Problems while opening file:" + ioexception.getMessage());
            return 0;
        }
        boolean flag = false;
        boolean flag1 = false;
        int j = 0;
        StringBuffer stringbuffer = new StringBuffer();
        try
        {
            dataoutputstream.writeBytes(/*"\n" +*/ taggedHeader(s));
            do
            {
                int i;
                if((i = bufferedinputstream.read()) <= 0)
                {
                    break;
                }
                if(flag1)
                {
                    dataoutputstream.write(i);
                    if(i == 10)
                    {
                        flag1 = false;
                        j = 0;
                        stringbuffer = new StringBuffer();
                    }
                } else
                if(i == 9)
                {
                    flag1 = true;
                } else
                if(i == 10)
                {
                    dataoutputstream.writeBytes(stringbuffer.toString());
                    dataoutputstream.write(i);
                    flag1 = false;
                    j = 0;
                    stringbuffer = new StringBuffer();
                } else
                if(++j < 0x10000)
                {
                    stringbuffer.append((char)i);
                } else
                if(j == 0x10000)
                {
                    stringbuffer = new StringBuffer();
                }
            } while(true);
        }
        catch(Exception exception)
        {
            System.err.println("Problems while io: " + exception.getMessage());
            exception.printStackTrace();
        }
        finally
        {
            try
            {
                bufferedinputstream.close();
                dataoutputstream.close();
            }
            catch(IOException ioexception1)
            {
                System.err.println("Problems while closing the file: " + ioexception1.getMessage());
            }
        }
        success++;
        return 1;
    }

    private boolean sentenceEndChar(char c)
    {
        if(c == '.' || c == '!' || c == '?')
        {
            return true;
        }
        return c == '"' || c == '\'' || c == '\253' || c == '\273';
    }

    public int processText(String s)
    {
        if(verbose)
        {
            System.err.println("[Text2Satz] Processing file: " + s);
        }
        boolean flag = false;
        int j = 0;
        boolean flag1 = false;
        StringBuffer stringbuffer = null;
        String s1 = "";
        Object obj = null;
        String s2 = "text2satz.tmp";
        String s3 = null;
        BufferedInputStream bufferedinputstream = null;
        BufferedInputStream bufferedinputstream1 = null;
        BufferedInputStream bufferedinputstream2 = null;
        DataOutputStream dataoutputstream = null;
        BreakIterator breakiterator = BreakIterator.getSentenceInstance(language);
        BreakIterator breakiterator1 = BreakIterator.getWordInstance(language);
        if(workingDir != null)
        {
            s2 = workingDir + File.separator + s2;
        }
        s1 = new String(s.substring(s.lastIndexOf('.') + 1));
        if(s1.length() > 5)
        {
            System.err.println("Warning: Ignoring file " + s);
            return 0;
        }
        if(s1.compareToIgnoreCase("txt") == 0)
        {
            s3 = new String(s);
        } else
        if(s1.toLowerCase().indexOf("htm") != -1)
        {
            if(htmlConverter != null && !htmlConverter.equals("internal"))
            {
                s3 = convertHtmlExternally(s, s2);
            } else
            {
                s3 = convertHtmlInternally(s, s2);
            }
            if(s3 == null)
            {
                return 0;
            }
        } else
        if(s1.compareToIgnoreCase("lit") == 0 || s1.compareToIgnoreCase("cyr") == 0 || s1.compareToIgnoreCase("cas") == 0)
        {
            if((s3 = convertLatin2(s, s2)) == null)
            {
                return 0;
            }
        } else
        if(s1.compareToIgnoreCase("doc") == 0)
        {
            if((s3 = convertDoc(s, s2)) == null)
            {
                return 0;
            }
        } else
        if(s1.compareToIgnoreCase("pdf") == 0)
        {
            if((s3 = convertPdf(s, s2)) == null)
            {
                return 0;
            }
        } else
        if(s1.compareToIgnoreCase("ceml") == 0)
        {
            if((s3 = convertCeml(s, s2)) == null)
            {
                return 0;
            }
        } else
        if(s1.toLowerCase().indexOf("xml") != -1)
        {
            if((s3 = convertHtmlInternally(s, s2)) == null)
            {
                return 0;
            }
        } else
        {
            System.err.println("Warning: Can't handle file type of file " + s);
            return 0;
        }
        File file = new File(s3);
        if(!file.canRead())
        {
            System.err.println("Can't open file: " + s);
            return 0;
        }
        String s4 = null;
        if(workingDir != null)
        {
            s4 = workingDir + File.separator + database + ".s";
        } else
        {
            s4 = database + ".s";
        }
        if(rawMode)
        {
            return printInRawMode(s, file, s4);
        }
        CharacterIteratorStream characteriteratorstream = new CharacterIteratorStream(s3);
        CharacterIteratorStream characteriteratorstream1 = new CharacterIteratorStream(s3);
        breakiterator.setText(characteriteratorstream);
        breakiterator1.setText(characteriteratorstream1);
        int l = breakiterator.first();
        try
        {
            bufferedinputstream = new BufferedInputStream(new FileInputStream(file));
            bufferedinputstream1 = new BufferedInputStream(new FileInputStream(file));
            bufferedinputstream2 = new BufferedInputStream(new FileInputStream(file));
            dataoutputstream = getOutStream(s4);
        }
        catch(IOException ioexception)
        {
            System.err.println("Problems while creating streams:" + ioexception.getMessage());
        }
        int i1 = 0;
        int j1 = 0;
        int k1 = 0;
        boolean flag2 = false;
        stringbuffer = new StringBuffer();
        boolean flag3 = false;
        boolean flag4 = true;
        boolean flag5 = true;
        try
        {
            dataoutputstream.writeBytes(/*"\n" +*/ taggedHeader(s));
            long l1 = l;
            long l6;
            do
            {
                l6 = bufferedinputstream.skip(l1);
            } while(l6 > 0L && (l1 -= l6) > 0L);
            i1 += l;
            for(int i2 = breakiterator.next(); i2 != -1; i2 = breakiterator.next())
            {
                int j2 = 0;
                for(int k2 = i2 - l; k2 > 0; k2--)
                {
                    int i = (char)bufferedinputstream.read();
                    i1++;
                    if(i == 10 || i == 13 || k1 == 0)
                    {
                        if(k1 <= i1)
                        {
                            long l2;
                            if(k1 == 0)
                            {
                                l2 = i1 - 1;
                                k1 = i1;
                            } else
                            {
                                l2 = i1 - k1;
                                k1 = i1 + 1;
                            }
                            long l7;
                            do
                            {
                                l7 = bufferedinputstream2.skip(l2);
                            } while(l7 > 0L && (l2 -= l7) > 0L);
                            stringbuffer.delete(0, stringbuffer.length());
                            j = (char)bufferedinputstream2.read();
                        }
                        while(j == 10 || j == 13) 
                        {
                            j = (char)bufferedinputstream2.read();
                            k1++;
                        }
                        if(j == 32)
                        {
                            stringbuffer.append((char)j);
                            j = (char)bufferedinputstream2.read();
                            k1++;
                            stringbuffer.append((char)j);
                        }
                        if(stringbuffer.toString().compareTo(" <") == 0)
                        {
                            stringbuffer.delete(0, stringbuffer.length());
                            int j3 = 0;
                            do
                            {
                                if((j = bufferedinputstream2.read()) == 62 || j <= 0)
                                {
                                    break;
                                }
                                k1++;
                                stringbuffer.append((char)j);
                            } while(++j3 <= 25 && j != 10);
                            k1++;
                            if(stringbuffer.toString().compareTo("quelle") == 0)
                            {
                                dataoutputstream.writeBytes("\n <" + stringbuffer + ">");
                                stringbuffer.delete(0, stringbuffer.length());
                                while((i = bufferedinputstream2.read()) != 10 && i > 0) 
                                {
                                    k1++;
                                    stringbuffer.append((char)i);
                                }
                                k1++;
                                stringbuffer.delete(0, stringbuffer.toString().indexOf("<name_lang>") + 11);
                                stringbuffer.delete(stringbuffer.toString().indexOf("</name_lang>"), stringbuffer.length());
                                if(stringbuffer.length() == 0)
                                {
                                    stringbuffer.append(s);
                                }
                                success++;
                                dataoutputstream.writeBytes("<name>" + database + Integer.toString(success + 1) + "</name><name_lang>" + stringbuffer + "</name_lang></quelle>\n");
                            } else
                            if(j == 62)
                            {
                                dataoutputstream.writeBytes("\n <" + stringbuffer + ">");
                                k1 += writeRestOfLine(dataoutputstream, bufferedinputstream2) + 1;
                            } else
                            {
                                dataoutputstream.writeBytes("\n <" + stringbuffer);
                                k1 += writeRestOfLine(dataoutputstream, bufferedinputstream2);
                            }
                            long l3 = k1 - i1 - 1;
                            long l8;
                            do
                            {
                                l8 = bufferedinputstream.skip(l3);
                            } while(l8 > 0L && (l3 -= l8) > 0L);
                            k2 -= k1 - i1 - 1;
                            for(i1 = k1 - 1; i1 > breakiterator.current();)
                            {
                                l = i2;
                                i2 = breakiterator.next();
                                k2 += i2 - l;
                            }

                        } else
                        if(i1 == l + 1)
                        {
                            dataoutputstream.write(i);
                        }
                        if(flag5)
                        {
                            continue;
                        }
                        if(keep_newline || flag4 && !flag5)
                        {
                            dataoutputstream.write(10);
                            flag5 = true;
                            flag3 = false;
                        } else
                        {
                            flag3 = true;
                        }
                        if(i != 13)
                        {
                            flag4 = true;
                        }
                        continue;
                    }
                    if(Character.isWhitespace((char)i))
                    {
                        if(flag5)
                        {
                            continue;
                        }
                        flag3 = true;
                        if(wrapLongLines && i2 - l - k2 - j2 > 220)
                        {
                            j2 = i2 - l - k2;
                            dataoutputstream.write(10);
                            flag5 = true;
                            flag3 = false;
                        }
                        continue;
                    }
                    if(i == 45)
                    {
                        long l4 = i1 - k1;
                        long l9;
                        do
                        {
                            l9 = bufferedinputstream2.skip(l4);
                        } while(l9 > 0L && (l4 -= l9) > 0L);
                        k1 = i1 + 1;
                        j = bufferedinputstream2.read();
                        if(j == 10 || j == 13)
                        {
                            int k3 = bufferedinputstream2.read();
                            k1++;
                            if((k3 == 10 || k3 == 13) && j != k3)
                            {
                                k3 = bufferedinputstream2.read();
                                k1++;
                            }
                            j = k3;
                            if(!Character.isLowerCase((char)j))
                            {
                                if(flag3)
                                {
                                    dataoutputstream.write(32);
                                }
                                dataoutputstream.write(i);
                            }
                            flag3 = flag4 = false;
                            flag5 = true;
                            continue;
                        }
                        if(flag3)
                        {
                            dataoutputstream.write(32);
                        }
                        dataoutputstream.write(i);
                        flag3 = flag4 = flag5 = false;
                        continue;
                    }
                    if(flag3)
                    {
                        dataoutputstream.write(32);
                    }
                    dataoutputstream.write(i);
                    flag3 = flag4 = flag5 = false;
                }

                if(breakiterator.current() > 2)
                {
                    int k = breakiterator1.preceding(breakiterator.current() - 2);
                    long l5 = k - j1;
                    if(l5 >= 0L)
                    {
                        stringbuffer.delete(0, stringbuffer.length());
                        long l10;
                        do
                        {
                            l10 = bufferedinputstream1.skip(l5);
                        } while(l10 > 0L && (l5 -= l10) > 0L);
                        for(int i3 = breakiterator1.next() - k; i3 > 0; i3--)
                        {
                            stringbuffer.append((char)bufferedinputstream1.read());
                        }

                        j1 = breakiterator1.current();
                        if(!abbreviations.contains(stringbuffer.toString()) && !flag5)
                        {
                            do
                            {
                                j = (char)bufferedinputstream1.read();
                                j1++;
                            } while(sentenceEndChar((char)j));
                            if(j != 44)
                            {
                                dataoutputstream.write(10);
                                flag5 = true;
                                flag3 = false;
                            }
                        }
                    }
                }
                l = i2;
            }

        }
        catch(Exception exception)
        {
            System.err.println("Problems while io: " + exception.getMessage());
            exception.printStackTrace();
        }
        try
        {
            characteriteratorstream.close();
            characteriteratorstream1.close();
            bufferedinputstream.close();
            bufferedinputstream1.close();
            bufferedinputstream2.close();
            dataoutputstream.close();
        }
        catch(IOException ioexception1)
        {
            System.err.println("Problems while closing the file: " + ioexception1.getMessage());
        }
        success++;
        return 1;
    }

    private String convertHtmlExternally(String s, String s1)
    {
        InputStream inputstream = null;
        try
        {
            htmlConverterCmdArray[htmlConverterArgPos] = s;
            inputstream = Runtime.getRuntime().exec(htmlConverterCmdArray).getInputStream();
        }
        catch(Exception exception)
        {
            System.err.println("Could not read output of " + htmlConverter + " '" + s + "': " + exception.getMessage());
            return null;
        }
        FileOutputStream fileoutputstream = null;
        try
        {
            fileoutputstream = new FileOutputStream(s1);
        }
        catch(IOException ioexception)
        {
            System.err.println("Could not open output file '" + s1 + "': " + ioexception.getMessage());
            return null;
        }
        try
        {
            byte abyte0[] = new byte[4096];
            int i = -1;
            boolean flag = false;
            while((i = inputstream.read(abyte0)) != -1) 
            {
                fileoutputstream.write(abyte0, 0, i);
            }
            fileoutputstream.close();
        }
        catch(IOException ioexception1)
        {
            System.out.println("Could not write output file '" + s1 + "': " + ioexception1.getMessage());
            try
            {
                fileoutputstream.close();
            }
            catch(Exception exception1) { }
            fileoutputstream = null;
            return null;
        }
        return s1;
    }

    private String convertHtmlInternally(String s, String s1)
    {
        BufferedReader bufferedreader = null;
        FileWriter filewriter = null;
        Html2Text html2text = null;
        try
        {
            html2text = new Html2Text();
        }
        catch(IOException ioexception)
        {
            System.err.println(getClass().getName() + ".convertHtml: error when creating converter: " + ioexception);
            ioexception.printStackTrace(System.err);
            return null;
        }
        try
        {
            bufferedreader = new BufferedReader(new FileReader(s));
        }
        catch(IOException ioexception1)
        {
            System.err.println(getClass().getName() + ".convertHtml: error when opening file " + s + ": " + ioexception1);
            ioexception1.printStackTrace(System.err);
            return null;
        }
        try
        {
            filewriter = new FileWriter(s1);
        }
        catch(IOException ioexception2)
        {
            System.err.println(getClass().getName() + ".convertHtml: error when opening file " + s1 + ": " + ioexception2);
            ioexception2.printStackTrace(System.err);
            return null;
        }
        try
        {
            html2text.filter(bufferedreader, filewriter);
            bufferedreader.close();
        }
        catch(Exception exception)
        {
            System.err.println(getClass().getName() + ".convertHtml: error during HTML to text conversion:" + exception);
            exception.printStackTrace(System.err);
        }
        return s1;
    }

    private String convertLatin2(String s, String s1)
    {
        InputStream inputstream = null;
        try
        {
            inputstream = Runtime.getRuntime().exec("7bit2lat2.pl " + s).getInputStream();
        }
        catch(Exception exception)
        {
            System.err.println("Could not read 7bit2lat2 output: " + exception.getMessage());
        }
        FileWriter filewriter = null;
        try
        {
            filewriter = new FileWriter(s1);
        }
        catch(Exception exception1)
        {
            System.err.println("Could not read 7bit2lat2 output: " + exception1.getMessage());
        }
        try
        {
            for(int i = 0; (i = inputstream.read()) != -1;)
            {
                filewriter.write(i);
            }

            filewriter.close();
        }
        catch(Exception exception2)
        {
            System.err.println("Could not write temporary file: " + exception2.getMessage());
            return null;
        }
        return s1;
    }

    private String convertDoc(String s, String s1)
    {
        InputStream inputstream = null;
        try
        {
            inputstream = Runtime.getRuntime().exec("wvHtml " + s + " " + s + ".html && lynx -dump -nolist -width=255 " + s + ".html").getInputStream();
        }
        catch(Exception exception)
        {
            System.err.println("Could not read converter output: " + exception.getMessage());
            return null;
        }
        FileWriter filewriter = null;
        try
        {
            filewriter = new FileWriter(s1);
        }
        catch(Exception exception1)
        {
            System.err.println("Could not read converter output: " + exception1.getMessage());
            return null;
        }
        try
        {
            for(int i = 0; (i = inputstream.read()) != -1;)
            {
                filewriter.write(i);
            }

            filewriter.close();
        }
        catch(Exception exception2)
        {
            System.err.println("Could not write temporary file: " + exception2.getMessage());
            return null;
        }
        return s1;
    }

    private String convertPdf(String s, String s1)
    {
        if(ConvertPdf.convertFile(s, s1))
        {
            return s1;
        } else
        {
            return null;
        }
    }

    private String convertCeml(String s, String s1)
    {
       
        return s1;
    }

    public static void main(String args[])
        throws IllegalArgumentException
    {
        String s = "abbrev";
        boolean flag = false;
        Text2Satz text2satz = new Text2Satz();
        String s1 = null;
        LongOpt alongopt[] = new LongOpt[13];
        alongopt[0] = new LongOpt("help", 0, null, 63);
        alongopt[1] = new LongOpt("language", 1, null, 76);
        alongopt[2] = new LongOpt("database", 1, null, 100);
        alongopt[3] = new LongOpt("abbrev", 2, null, 97);
        alongopt[4] = new LongOpt("keep_newline", 0, null, 110);
        alongopt[5] = new LongOpt("version", 0, null, 86);
        alongopt[6] = new LongOpt("files", 1, null, 102);
        alongopt[7] = new LongOpt("html", 1, null, 104);
        alongopt[8] = new LongOpt("dbconnection", 1, null, 109);
        alongopt[9] = new LongOpt("path", 1, null, 112);
        alongopt[10] = new LongOpt("raw", 0, null, 114);
        alongopt[11] = new LongOpt("wrap", 0, null, 119);
        alongopt[12] = new LongOpt("verbose", 0, null, 118);
        Getopt getopt = new Getopt("text2Satz", args, "?L:d:a:nVf:h:m:p:rwv", alongopt);
        getopt.setOpterr(false);
        do
        {
            int i;
            if((i = getopt.getopt()) == -1)
            {
                break;
            }
            switch(i)
            {
            case 100: // 'd'
                text2satz.setDatabase(getopt.getOptarg());
                break;

            case 104: // 'h'
                text2satz.setHtmlConverter(getopt.getOptarg());
                break;

            case 97: // 'a'
                s = getopt.getOptarg();
                break;

            case 102: // 'f'
                s1 = getopt.getOptarg().trim();
                break;

            case 76: // 'L'
                String s2 = getopt.getOptarg();
                if(s2.startsWith("de"))
                {
                    text2satz.setLanguage(Locale.GERMANY);
                } else
                if(s2.startsWith("en"))
                {
                    text2satz.setLanguage(Locale.US);
                } else
                if(s2.startsWith("fr"))
                {
                    text2satz.setLanguage(Locale.FRANCE);
                } else
                if(s2.startsWith("it"))
                {
                    text2satz.setLanguage(Locale.ITALY);
                }
                break;

            case 109: // 'm'
                text2satz.setDbConnectionName(getopt.getOptarg());
                break;

            case 110: // 'n'
                text2satz.setKeepNewline(true);
                break;

            case 112: // 'p'
                text2satz.setWorkingDir(getopt.getOptarg());
                break;

            case 114: // 'r'
                text2satz.setRawMode(true);
                break;

            case 118: // 'v'
                text2satz.verbose = true;
                break;

            case 86: // 'V'
                System.out.println("text2satz 1.1");
                flag = true;
                break;

            case 119: // 'w'
                text2satz.setWrapLongLines(true);
                break;

            case 63: // '?'
                flag = true;
                break;
            }
        } while(true);
        if(flag)
        {
            usage();
        }
        text2satz.readAbbrevs(s);
        int j = getopt.getOptind();
        if(j < args.length)
        {
            for(j = j; j < args.length; j++)
            {
                text2satz.processText(args[j]);
            }

        } else
        {
            String s3 = null;
            BufferedReader bufferedreader = null;
            if(s1 == null)
            {
                bufferedreader = new BufferedReader(new InputStreamReader(System.in));
            } else
            {
                try
                {
                    bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(s1)));
                }
                catch(FileNotFoundException filenotfoundexception)
                {
                    System.out.println("Could not read filenames from file " + s1);
                    System.out.println(filenotfoundexception.getMessage());
                }
            }
            try
            {
                while((s3 = bufferedreader.readLine()) != null) 
                {
                    text2satz.processText(s3);
                }
            }
            catch(Exception exception)
            {
                System.err.println("Could not read filenames from stdin: " + exception.getMessage());
            }
        }
        j = text2satz.getNrOfTextsProcessed();
        if(j == 0)
        {
            usage();
        } else
        {
            System.err.println("Segmented " + j + " texts into sentences.");
        }
    }

}
