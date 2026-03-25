package mpi.eudico.client.im.spi.lookup;

import java.awt.Image;
import java.awt.im.spi.InputMethod;
import java.awt.im.spi.InputMethodDescriptor;

import java.util.Locale;


/**
 * A basic implementation of a {@code InputMethodDescriptor}.
 */
public class LookupDescriptor implements InputMethodDescriptor {
    /**
     * Creates a new LookupDescriptor instance
     */
    public LookupDescriptor() {
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getAvailableLocales
     */
    @Override
	public Locale[] getAvailableLocales() {
        return Lookup2.SUPPORTED_LOCALES;
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#hasDynamicLocaleList
     */
    @Override
	public boolean hasDynamicLocaleList() {
        return false;
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodDisplayName
     */
    @Override
	public synchronized String getInputMethodDisplayName(Locale il, Locale dl) {
        return "mpi.nl";
    }

    /**
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodIcon
     */
    @Override
	public Image getInputMethodIcon(Locale inputLocale) {
        return null;
    }

    /*
     * @see java.awt.im.spi.InputMethodDescriptor#getInputMethodClassName
     */
    /*
    public String getInputMethodClassName() {
        return null;
    }
	*/
    /**
     * Creates a new Lookup2 instance.
     *
     * @return the input method
     *
     * @throws Exception any exception
     */
    @Override
	public InputMethod createInputMethod() throws Exception {
        return new Lookup2();
    }
}
