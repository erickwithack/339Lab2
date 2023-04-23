package simpledb.storage;

import simpledb.common.Database;
import simpledb.common.DbException;
import simpledb.common.Debug;
import simpledb.common.Permissions;
import simpledb.transaction.TransactionAbortedException;
import simpledb.transaction.TransactionId;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * HeapFile is an implementation of a DbFile that stores a collection of tuples
 * in no particular order. Tuples are stored on pages, each of which is a fixed
 * size, and the file is simply a collection of those pages. HeapFile works
 * closely with HeapPage. The format of HeapPages is described in the HeapPage
 * constructor.
 *
 * @author Sam Madden
 * @see HeapPage#HeapPage
 */
public class HeapFile implements DbFile {

    /**
     * Constructs a heap file backed by the specified file.
     *
     * @param f the file that stores the on-disk backing store for this heap
     *          file.
     */
    private final TupleDesc tupleDesc;
	private final File file;
	private final int tableId;

    public HeapFile(File f, TupleDesc td) {
        // TODO: some code goes here
        tupleDesc = td;
    	file = f;
    	tableId = f.getAbsolutePath().hashCode();
    }

    /**
     * Returns the File backing this HeapFile on disk.
     *
     * @return the File backing this HeapFile on disk.
     */
    public File getFile() {
        // TODO: some code goes here
        return file;
    }

    /**
     * Returns an ID uniquely identifying this HeapFile. Implementation note:
     * you will need to generate this tableid somewhere to ensure that each
     * HeapFile has a "unique id," and that you always return the same value for
     * a particular HeapFile. We suggest hashing the absolute file name of the
     * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
     *
     * @return an ID uniquely identifying this HeapFile.
     */
    public int getId() {
        // TODO: some code goes here
        return file.getAbsolutePath().hashCode();
    }

    /**
     * Returns the TupleDesc of the table stored in this DbFile.
     *
     * @return TupleDesc of this DbFile.
     */
    public TupleDesc getTupleDesc() {
        // TODO: some code goes here
        return tupleDesc;
    }

    // see DbFile.java for javadocs
    public Page readPage(PageId pid) {
        // TODO: some code goes here
        HeapPageId id = (HeapPageId) pid;
        BufferedInputStream bis = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(file));
            byte pageBuf[] = new byte[BufferPool.getPageSize()];
            System.out.println("Seeking to page " + id.getPageNumber());
            if (bis.skip(id.getPageNumber() * BufferPool.getPageSize()) != id
                    .getPageNumber() * BufferPool.getPageSize()) {
                throw new IllegalArgumentException(
                        "Unable to seek to correct place in heapfile");
            }
            int retval = bis.read(pageBuf, 0, BufferPool.getPageSize());
            if (retval == -1) {
                throw new IllegalArgumentException("Read past end of table");
            }
            if (retval < BufferPool.getPageSize()) {
                throw new IllegalArgumentException("Unable to read "
                        + BufferPool.getPageSize() + " bytes from heapfile");
            }
            Debug.log(1, "HeapFile.readPage: read page %d", id.getPageNumber());
            HeapPage p = new HeapPage(id, pageBuf);
            return p;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Close the file on success or error
            try {
                if (bis != null)
                    bis.close();
            } catch (IOException ioe) {
                // Ignore failures closing the file
            }
        }

    }

    // see DbFile.java for javadocs
    public void writePage(Page page) throws IOException {
        // TODO: some code goes here
        // not necessary for lab1
    }

    /**
     * Returns the number of pages in this HeapFile.
     */
    public int numPages() {
        // TODO: some code goes here
        return (int) (file.length() / BufferPool.getPageSize());
    }

    // see DbFile.java for javadocs
    public List<Page> insertTuple(TransactionId tid, Tuple t)
            throws DbException, IOException, TransactionAbortedException {
        // TODO: some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public List<Page> deleteTuple(TransactionId tid, Tuple t) throws DbException,
            TransactionAbortedException {
        // TODO: some code goes here
        return null;
        // not necessary for lab1
    }

    // see DbFile.java for javadocs
    public DbFileIterator iterator(TransactionId tid) {
        // TODO: some code goes here
        return new HeapFileIterator(this, tid);
    }

}

class HeapFileIterator extends AbstractDbFileIterator {

    Iterator<Tuple> it = null;
    int curpgno = 0;

    TransactionId tid;
    HeapFile hf;

    public HeapFileIterator(HeapFile hf, TransactionId tid) {
        this.hf = hf;
        this.tid = tid;
    }

    public void open() throws DbException, TransactionAbortedException {
        curpgno = -1;
    }

    protected Tuple readNext() throws TransactionAbortedException, DbException {
        if (it != null && !it.hasNext())
            it = null;

       // System.out.println("Working from hf.numpages = " + hf.numPages() + " with a curpage " + curpgno);
        while (it == null && curpgno < hf.numPages() - 1) {
            curpgno++;
            HeapPageId curpid = new HeapPageId(hf.getId(), curpgno);
            HeapPage curp = (HeapPage) Database.getBufferPool().getPage(tid,
                    curpid, Permissions.READ_ONLY);
            it = curp.iterator();
            if (!it.hasNext()) {
                it = null;
            }
            
        }

       
        if (it == null)
            return null;
        return it.next();
    }

    public void rewind() throws DbException, TransactionAbortedException {
        close();
        open();
    }

    public void close() {
        super.close();
        it = null;
        curpgno = Integer.MAX_VALUE;
    }
}