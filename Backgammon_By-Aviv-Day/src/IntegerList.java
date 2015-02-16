
/**
 * A List-Type for the basic type integer.
 *
 * @author Aviv
 */
public class IntegerList {

    // the stored data
    private int array[];
    // the size of this list
    private int size;

    /**
     * create a new IntList and initially provide space for a given number
     * of int-Values.
     *
     * If this number is reached new space is allocated automatically.
     * @param initsize the number of int-Values to initially provide space for.
     */
    public IntegerList(int initsize) {
        size = 0;
        array = new int[initsize];
    }

    /**
     * create a new IntList with an inital space of 10.
     */
    public IntegerList() {
        this(10);
    }

    /**
     * create a new IntList from an int-array. This array is cloned and the
     * clone used as underlying data.
     * @param a an int-array which must not be null
     */
    public IntegerList(int[] a) {
        array = (int[])a.clone();
        size = array.length;
    }

    /**
     * create a new IntList from a part of an array of int.
     * @param a array to take from
     * @param start index to start at
     * @param length number of int-values to copy.
     */
    public IntegerList(int[] a, int start, int length) {
        array = new int[length];
        for (int i = 0; i < length; i++) {
            array[i] = a[start+i];
        }
        size = length;
    }

    /**
     * add an int-value to the end of the list.
     * @param value int value to add
     */
    public void addInteger(int value) {
        if (size == array.length) {
            int[] old = array;
            array = new int[old.length + 10];
            System.arraycopy(old, 0, array, 0, old.length);
        }
        array[size++] = value;
    }

    /**
     * remove the intvalue at a given index
     * @param pos index to be removed.
     * @throws IndexOutOfBoundsException
     */
    public void removeByIndex(int pos) {
        if (pos >= size) {
            throw new IndexOutOfBoundsException("remove: " + pos);
        }
        for (int i = pos; i < size - 1; i++) {
            array[i] = array[i + 1];
        }
        size--;
    }

    /**
     * remove a value from this IntList. Remove it at most once.
     * @param val int-value to be removed
     * @return true iff the value has been found and removed
     */
    public boolean removeInteger(int val) {
        for (int i = 0; i < size; i++) {
            if (array[i] == val) {
                removeByIndex(i);
                return true;
            }
        }
        return false;
    }

    /**
     * get the value at a certain index.
     * @param index index to get the value of
     * @return int-value at this index
     * @throws IndexOutOfBoundsException
     */
    public int at(int index) {
        return array[index];
    }

    /**
     *
     * @return the number of int-values in this IntList
     */
    public int length() {
        return size;
    }

    /**
     * remove the maximum from the IntList. remove it only once.
     */
    public void removeMax() {
        int max = Integer.MIN_VALUE;
        int maxpos = 0;
        for (int i = 0; i < size; i++) {
            if (array[i] > max) {
                max = array[i];
                maxpos = i;
            }
        }
        removeByIndex(maxpos);
    }

    /**
     * is a certain int-value present in the IntList
     * @param value value to searched
     * @return true iff value is in this IntList.
     */
    public boolean contains(int value) {
        for (int i = 0; i < size; i++) {
            if (array[i] == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * the maximum in this list.
     * @return int that is ">=" to all elements in the list
     */
    public int max() {
        int m = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            if (array[i] > m) {
                m = array[i];
            }
        }
        return m;
    }

    /**
     * clone this list. The underlying array is deepcopied.
     * @return Object
     */
    public Object clone() {
        return new IntegerList(array, 0, size);
    }

    /**
     * get an IntList that contains every number contained in this but is minimal
     *
     * @return IntList the unique Sub-IntList
     */
    public IntegerList distinctValues() {
        IntegerList ret = new IntegerList();
        for (int i = 0; i < size; i++) {
            if(!ret.contains(array[i]))
                ret.addInteger(array[i]);
        }
        return ret;
    }

}
