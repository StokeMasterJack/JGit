package org.spearce.egit.ui;

import org.eclipse.osgi.util.NLS;

public class UIText extends NLS {
    public static String SharingWizard_windowTitle;

    public static String ExistingRepositoryPage_title;

    public static String ExistingRepositoryPage_description;

    public static String ExistingRepositoryPage_mainText;

    static {
        NLS.initializeMessages(UIText.class.getPackage().getName() + ".uitext",
                UIText.class);
    }
}