package tfd.gui.swing;


import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

/**
 * Class to provide the ability to right click on a JTextComponent
 * and render a Popup with Cut/Copy/Paste/Delete/Select All options
 * Options are enable based on context.
 *  - Cut and Delete are enabled when text is selected and editable.
 *  - Copy is enabled when test is selected.
 *  - Paste is enabled when clipboard has string contents and text is editable
 *  - Select All is enabled when text length > 0 
 * @author daltont
 *
 */
public class CutCopyPastePopupSupport {
    private static JPopupMenu   popUp;
    private static JMenuItem    cutItem;
    private static JMenuItem    copyItem;
    private static JMenuItem    pasteItem;
    private static JMenuItem    deleteItem;
    private static JMenuItem    selectAllItem;

    private static MouseHandler mouseHandler;

    private CutCopyPastePopupSupport() { } // Prevent instantiation
    
    public static void enable(JTextComponent jtc) {
        if (popUp == null) {
            popUp = new JPopupMenu();

            AbstractAction action = null;

            cutItem = new JMenuItem("Cut");
            cutItem.setMnemonic('t');
            // Since DefaultEditorKit didn't have all the actions needed,
            // All are provided here for consistancy. Code for DefaultEditorKit
            // is commented out.

            // action = new DefaultEditorKit.CutAction();
            action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) { 
                    JTextComponent txtComponent = (JTextComponent) popUp.getInvoker();
                    txtComponent.cut();
                    //Clipboard clipbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    //StringSelection ss = new StringSelection(txtComponent.getSelectedText());
                    //clipbrd.setContents(ss,ss);
                    //txtComponent.replaceSelection("");
                }
            };
            cutItem.addActionListener(action);
            popUp.add(cutItem);

            copyItem = new JMenuItem("Copy");
            copyItem.setMnemonic('C');
            // action = new DefaultEditorKit.CopyAction();
            action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    JTextComponent txtComponent = (JTextComponent) popUp.getInvoker();
                    txtComponent.copy();
                    //Clipboard clipbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    //StringSelection ss = new StringSelection(txtComponent.getSelectedText());
                    //clipbrd.setContents(ss,ss);
                }
            };
            copyItem.addActionListener(action);
            popUp.add(copyItem);

            pasteItem = new JMenuItem("Paste");
            pasteItem.setMnemonic('P');
            //action = new DefaultEditorKit.PasteAction();
            action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    JTextComponent txtComponent = (JTextComponent) popUp.getInvoker();
                    txtComponent.paste();
                    //Clipboard clipbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    //try {
                    //    txtComponent.replaceSelection((String) clipbrd.getContents(this).getTransferData(DataFlavor.stringFlavor));
                    //} catch (UnsupportedFlavorException usfex) { } // shouldn't happen
                    //  catch (IOException ioex) { } // shouldn't happen either

                }
            };
            pasteItem.addActionListener(action);
            popUp.add(pasteItem);

            deleteItem = new JMenuItem("Delete");
            deleteItem.setMnemonic('D');
            // DefaultEditorKit does not have a DeleteAction
            action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    JTextComponent txtComponent = (JTextComponent) popUp.getInvoker();
                    txtComponent.replaceSelection("");
                }
            };
            deleteItem.addActionListener(action);
            popUp.add(deleteItem);

            popUp.addSeparator();

            selectAllItem = new JMenuItem("Select All");
            selectAllItem.setMnemonic('A');

            // DefaultEditorKit had inner class SelectAllAction,
            // but it wasn't public as of Java 1.6
            action = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    JTextComponent txtComponent = (JTextComponent) popUp.getInvoker();
                    txtComponent.selectAll();
                }
            };
            selectAllItem.addActionListener(action);

            popUp.add(selectAllItem);
        }
        if (mouseHandler == null) {
            mouseHandler = new MouseHandler();
        }
        // A little defensive programming to prevent the mouseHandler
        // to being attached more than once.
        MouseListener[] mouseListeners = jtc.getMouseListeners();
        for (int i=0 ; i < mouseListeners.length; i++ ) {
            if (mouseListeners[i] == mouseHandler) {
                return;
            }
        }
        jtc.addMouseListener(mouseHandler);
    }

    private static class MouseHandler extends MouseAdapter {

        public void mouseReleased(MouseEvent me) {
            if (me.isPopupTrigger()) { 
                JTextComponent txtComponent = (JTextComponent) me.getSource();
                txtComponent.requestFocus();

                // determine what option are available
                boolean editable = txtComponent.isEditable() && txtComponent.isEnabled();

                if (txtComponent.getSelectedText() == null) {
                    cutItem.setEnabled(false);
                    copyItem.setEnabled(false);
                    deleteItem.setEnabled(false);
                } else {
                    cutItem.setEnabled(editable);
                    copyItem.setEnabled(true);
                    deleteItem.setEnabled(editable);
                }

                selectAllItem.setEnabled(txtComponent.getDocument().getLength() > 0);

                try {
                    Clipboard clipbrd = Toolkit.getDefaultToolkit().getSystemClipboard();
                    if (clipbrd.getContents(this).getTransferData(DataFlavor.stringFlavor) == null) {
                        pasteItem.setEnabled(false);
                    } else {
                        pasteItem.setEnabled(editable);
                    }
                } catch (Exception ex) {
                    pasteItem.setEnabled(false);
                }

                Point mouse_point = me.getPoint();
                popUp.setInvoker(txtComponent);
                popUp.show(me.getComponent(), mouse_point.x, mouse_point.y);
            }
        }
    }
}
