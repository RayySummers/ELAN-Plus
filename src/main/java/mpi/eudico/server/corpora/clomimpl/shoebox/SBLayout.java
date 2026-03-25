package mpi.eudico.server.corpora.clomimpl.shoebox;


import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mpi.eudico.server.corpora.clom.Annotation;
import mpi.eudico.server.corpora.clom.Tier;
import mpi.eudico.server.corpora.clomimpl.abstr.TierImpl;
import mpi.eudico.server.corpora.clomimpl.abstr.TranscriptionImpl;
import mpi.eudico.server.corpora.clomimpl.shoebox.utr22.SimpleConverter;


/**
 * Computes a "Shoebox" style of layout.
 */
public class SBLayout {
    // ref to refer tier

    private Tier _ref_tier;

   
    private List<Annotation> _vBlockOrder;

   
    private int _nBlockPos;

   
    private TranscriptionImpl _transcription;
    private ShoeboxTypFile _sbxtf;
    private SimpleConverter _simpleConverter;

    // the current ref tag that is in current view time

   
    private List<Annotation> _vRefTags;

   
    private Annotation _ref_tag;

    // pos in the the Reference tier that the current ref_tag is from

   
    private int _ref_tag_pos;
    private Writer _writer;
    //private List<SBTag> _vSBTags = new ArrayList<SBTag>();

    /**
     * Constructor for SBLayout will create a layout based on the first segment in
     * the reference tier
     *
     * @param trans the transcription
     */
    public SBLayout(TranscriptionImpl trans) {
        _transcription = trans;

        try {
            _simpleConverter = new SimpleConverter(null);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Constructor with a transcription and a Shoebox .typ file.
     * @param trans Transcription
     * @param sbxtf a Shoebox Typ file (contains characterset of a shoebox
     *        tier)
     */
    public SBLayout(TranscriptionImpl trans, ShoeboxTypFile sbxtf) {
        this(trans);
        _sbxtf = sbxtf;
    }

    /**
     * gets all the reference tiers
     * and sorts them based on their begin times
     * this is the order that the blocks will be displayed
     *
     */
    public void getRefTierOrder() {
        if (_transcription == null) {
        	_vBlockOrder = new ArrayList<Annotation>();
        	return;
        }
        List<AnnotationContainer> vSort = new ArrayList<AnnotationContainer>();
        int i;

        try {
            List<TierImpl> v = _transcription.getTopTiers();

            for (i = 0; i < v.size(); i++) {
                TierImpl ti = v.get(i);

                for (Annotation ann : ti.getAnnotations()) {
                    vSort.add(new AnnotationContainer(ann));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(vSort);

        List<Annotation> vecRet = new ArrayList<Annotation>();

        for (i = 0; i < vSort.size(); i++) {
            Annotation a = vSort.get(i).getAnnotation();
            vecRet.add(a);
        }

        _vBlockOrder = vecRet;
    }

    /**
     * Creates the segmentation order.
     */
    public void getSegOrder() {
        getRefTierOrder(); // not here;
        _nBlockPos = 0;
    }

    // sets the current working segment to show
    // range - howmany segs to show
    // pos = -1 back , 1 forward
    /**
     * Sets the segments range.
     * 
     * @param size the size
     * @param pos the position back or forward
     */
    public void setWorkingSegmentsRange(int size, int pos) {
        List<Annotation> retVec = new ArrayList<Annotation>();

        if (_nBlockPos >= _vBlockOrder.size()) {
            if (_nBlockPos > _vBlockOrder.size()) {
                _nBlockPos = _vBlockOrder.size() - 1;
            }
        }

        for (int i = 0; i < size; i++) {
            if (pos == 1) {
                _nBlockPos++;
            } else {
                _nBlockPos--;
            }

            if (_nBlockPos >= _vBlockOrder.size()) {
                _nBlockPos = _vBlockOrder.size() - 1;
            }

            if (_nBlockPos < 0) {
                _nBlockPos = 0;
            }

            retVec.add(_vBlockOrder.get(_nBlockPos));
        }

        if (retVec.size() > 0) {
            _vRefTags = retVec;
        }
    }

    /**
     * Sets blocks at the specified time as visible.
     * @param time the time to show
     * @return whether there are any segments at the time
     */
    public boolean setBlocksVisibleAtTime(long time) {
        // first check where we are at
        Annotation af = null;
        Annotation al = null;

        // HS 18 nov 03 - prevent NullPointerException at this point
        if ((_vRefTags == null) || (_vRefTags.size() == 0)) {
            return false;
        }

        af = _vRefTags.get(0);
        al = _vRefTags.get(_vRefTags.size() - 1);

        if ((af.getBeginTimeBoundary() <= time) &&
                (al.getEndTimeBoundary() >= time)) {
            return false;
        }

        boolean bf = false;
        long lasttime = -1;

        while (af.getEndTimeBoundary() < time) {
            // go back
            bf = true;

            if (lasttime == af.getEndTimeBoundary()) {
                return false;
            }

            lasttime = af.getEndTimeBoundary();

            setWorkingSegmentsRange(1, 1);
            af = _vRefTags.get(0);
            al = _vRefTags.get(_vRefTags.size() - 1);
        }

        if (bf) {
            //buildLayout();
            return true;
        }

        lasttime = -1;

        while (al.getBeginTimeBoundary() > time) {
            //go forward
            bf = true;

            if (lasttime == af.getBeginTimeBoundary()) {
                return false;
            }

            lasttime = af.getBeginTimeBoundary();

            setWorkingSegmentsRange(1, -1);
            af = _vRefTags.get(0);
            al = _vRefTags.get(_vRefTags.size() - 1);
        }

        if (bf) {
            return true;
        }

        return false;
    }

    /**
     * Returns a list of annotations between the specified times.
     * 
     * @param tier the tier to search
     * @param start the start time
     * @param end the end time
     * @return a list of annotations or null
     */
    protected List<Annotation> getAnnBetweenTime(Tier tier, long start, long end) {
        List<? extends Annotation> v = null;
        TierImpl ti = (TierImpl) tier;

        List<Annotation> vecRet = new ArrayList<Annotation>();

        try {
            v = ti.getAnnotations();
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }

        for (int i = 0; i < v.size(); i++) {
            Annotation a = v.get(i);

            if ((a.getBeginTimeBoundary() >= start) &&
                    (a.getEndTimeBoundary() <= end)) {
                vecRet.add(a);
            }
        }

        return vecRet;
    }

    /**
     * Returns current reference segment start time.
     *
     * @return long time value
     */
    public long getCurrentRefStartTime() {
        if (_ref_tag == null) {
            return 0;
        }

        return (_ref_tag.getBeginTimeBoundary());
    }

    /**
     * Returns current reference segment end time
     *
     * @return long time value
     */
    public long getCurrentRefEndTime() {
        if (_ref_tag == null) {
            return 0;
        }

        return (_ref_tag.getEndTimeBoundary());
    }

    /**
     * Sets reference tier to the prev Annotation based on the current _ref_tag
     *
     * @return true for is next otherwise false
     */
    public boolean getPrevRef() {
        Annotation an = null;

        if (_ref_tier == null) {
            return false;
        }

        an = ((TierImpl) _ref_tier).getAnnotationBefore(_ref_tag);

        if (an == null) {
            return false;
        }

        return true;
    }

    /**
     * This is WRONG it breaks the ability to return more then one block in a
     * view it is here for a quick hack to get the new layout stuff working
     *
     * @return the annotation
     */
    public Annotation getRefAnn() {
        Annotation refann = _vBlockOrder.get(_nBlockPos);

        return refann;
    }

    /**
     * Very temporary method to avoid ArrayIndexOutOfBoundsExceptions in Elan
     * 2.0  without introducing NullPointerExceptions (in older and current
     * Elan version). This is no good...
     *
     * @return whether it is save or not to call getRefAnn()
     */
    public boolean isRefAnnAvailable() {
        if (_vBlockOrder == null) {
            return false;
        }

        return ((_nBlockPos < _vBlockOrder.size()) && !(_nBlockPos < 0));
    }

    /**
     * Exports all annotations to the specified file.
     * @param filename the output file
     * @param header a header line
     */
    public void exportAll(String filename, String header) {
        try {
            openWriter(filename); // open fp for writting
            getSegOrder(); // get all segments or blocks
            write(header + "\r\r\n");

            for (int i = 0; i < _vBlockOrder.size(); i++) {
                Annotation refann = _vBlockOrder.get(i);
                AnnotationSize as = new AnnotationSize(_transcription, refann);
                List<TierImpl> vtiers = as.getTiers();

                for (Tier tier : vtiers) {

                    // write tier name
                    write(chopAtChar(tier.getName()) + " ");

                    boolean isSILIPAcharacterset = (_sbxtf != null) &&
                        _sbxtf.isIPAtier(chopAtChar(tier.getName()));

                    //List tieranns = as.getTierLayoutInChar(tier);
                    //Enumeration ee = tieranns.elements();
                    // changed HS 19-01-2004
                    List<AnnotationSizeContainer> tierAnn = as.getTierLayoutInPixels(tier, null);

                    for (AnnotationSizeContainer asc : tierAnn) {
                        Annotation a = asc.getAnnotation();
                        String wstr = "";

                        if (a == null) {
                            wstr = (padString("", asc.getSize() + 1));
                        } else {
                            String trimedValueofa = a.getValue().trim();

                            if (isSILIPAcharacterset) {
                                trimedValueofa = _simpleConverter.toBinary(trimedValueofa);
                            }

                            wstr = padString(trimedValueofa, asc.getSize() + 1);
                        }

                        // write annotation
                        write(padString(wstr, asc.getSize()));

                        // write the tier speaker and 
                        // time stamp block when the 
                        // current annotation is the 
                        // ref annotation
                        writeBlockStamp(a, refann);
                    }

                    write("\r\n");
                }

                write("\r\n");
            }

            _writer.flush();
            _writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /*
       public StringBuilder exportAll2(String filename,String header)
       {
           try {
           openWriter(filename);
           getSegOrder(); // get all segments or blocks
           write(header+"\n");
           setWorkingSegmentsRange(1,0); // set to load 1 block at a time at n block
           exportSeg();
           for (int i = 0 ; i < _vBlockOrder.size()-1; i++)
           {
               setWorkingSegmentsRange(1,1); // set to load 1 block at a time at n block
               exportSeg();
               write("\r\r\n");
           }
               _writer.flush();
               _writer.close();
           } catch(Exception e) {}
           return null;
       }
       private StringBuilder exportSeg()
       {
           try {
           List v = buildAllTiers(); // get all tiers for current block
           Enumeration e = v.elements(); // get enumeration of current tier/block
           while(e.hasMoreElements())
           {
               Enumeration eVisTier = null;
               Hashtable ht = (Hashtable) e.nextElement(); // get next tier in block
               eVisTier = _transcription.getTiers(null).elements(); // get all the tiers
    
               while(eVisTier.hasMoreElements())
               {
                   Tier tiercurrent = (Tier)eVisTier.nextElement();
                   List vTier = (List) ht.get(tiercurrent);  // get all tags at tiercurrent
                   if (vTier != null)
                   {
                       // write the tier name
                       write(chopAtChar(tiercurrent.getName())+" ");
                       // make enum of tier elements
                       Enumeration eTier = vTier.elements();
                       String wkstr = "";
                       int sz = 0;
                       Annotation last = null;
                       // loop thu all the elements
                       while(eTier.hasMoreElements())
                       {
                           Annotation a = null;
                           Object obj = (Object)eTier.nextElement();
                           if (obj instanceof Annotation)
                           {
                               a = (Annotation) obj;
                               // get spacing of tag
                               sz = _annSize.getAnnotationGroupSpacing(_transcription,a)+1;
                               wkstr = a.getValue();
                               last = a;
    
                           } else if( obj instanceof Long) {
                               Long l = (Long) obj;
                               sz = (_annSize.getSpacingForTime(_transcription,last,l.longValue()))+1; // get spacing of blank item
                               wkstr = "";
                           }
                           // write annotation
                           write(padString(wkstr,sz));
                           // write the tier speaker and
                           // time stamp block when the
                           // current annotation is the
                           // ref annotation
                           //writeBlockStamp(a);
                       }
                      }
               write("\r\r\n");
               }
           }
           } catch (Exception ex) {
               ex.printStackTrace();
               return null;
           }
           return null;
       }
     */
    private String padString(String orig, int totallen) {
    	orig = orig.trim();
    	
        int len = totallen - orig.length();

        if (len < 0) {
            return orig;
        }

        for (int i = 0; i < len; i++) {
			orig += " ";
		}

        return orig;
    }

    private String chopAtChar(String str) {
        int in = str.indexOf("@");

        if (in == -1) {
            return addSlash(str);
        }

        return addSlash(str.substring(0, in));
    }

    private String addSlash(String str) {
        int in = str.indexOf("\\");

        if (in == -1) {
            str = "\\" + str;
        }

        return str;
    }

    private String getSpeakerFormat(String str) {
        int in = str.indexOf("@");

        if (in == -1) {
            return "\\EUDICOp unknown";
        }

        return "\\EUDICOp " + str.substring(in + 1);
    }

    private void writeBlockStamp(Annotation a, Annotation refann)
        throws IOException {
        if (a == null) {
            return;
        }

        if (a.equals(refann)) {
            String wkstr = "\r\n";

            try {
                Tier t = a.getTier();
                wkstr += (getSpeakerFormat(t.getName()) + "\r\n");
            } catch (Exception e) {
            }

            wkstr += ("\\EUDICOt0 " + (a.getBeginTimeBoundary() * .001) +
            "\r\n\\EUDICOt1 " + (a.getEndTimeBoundary() * .001));
            write(wkstr + "\r\n");
        }

        return;
    }

    private void openWriter(String file) throws IOException {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            _writer = new OutputStreamWriter(fos, "ISO-8859-1");
        } catch (IOException ioe) {
            throw ioe;
        }
    }

    private void write(String text) throws IOException {
        try {
            _writer.write(text);
        } catch (IOException ioe) {
            throw ioe;
        }
    }

    /**
     * An {@link Annotation} container for comparing annotations based on their
     * begin time.
     * <p>
     * Note: this class has a natural ordering that is inconsistent with equals.
     */
    public class AnnotationContainer implements Comparable<AnnotationContainer> {
       
        Annotation _ann;

        /**
         * Creates a new AnnotationContainer instance
         *
         * @param a the annotation
         */
        public AnnotationContainer(Annotation a) {
            _ann = a;
        }

        /**
         * Returns the annotation.
         * 
         * @return the annotation
         */
        public Annotation getAnnotation() {
            return _ann;
        }

        @Override
		public int compareTo(AnnotationContainer obj) {
            AnnotationContainer ac = obj;
            Annotation a = ac.getAnnotation();

            if (_ann.getBeginTimeBoundary() > a.getBeginTimeBoundary()) {
                return 1;
            }

            if (_ann.getBeginTimeBoundary() < a.getBeginTimeBoundary()) {
                return -1;
            }

            return 0;
        }
    }
}
