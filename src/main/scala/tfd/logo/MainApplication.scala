package tfd.logo

import javax.swing.{AbstractAction, ImageIcon, JButton, JFileChooser, JFrame, JScrollPane, JSplitPane, JOptionPane, JTextArea, JToolBar, SwingWorker}
import javax.swing.filechooser.{FileNameExtensionFilter}
import java.awt.{BorderLayout, Dimension}
import java.awt.event.{ActionEvent}
import java.io.{BufferedReader, BufferedWriter, File, FileReader, FileWriter}

import tfd.gui.swing.CutCopyPastePopupSupport

class MainApplication {
	var currentFile:File = _
  
	val canvas = new DrawCanvas

    val codeText = new JTextArea();
    { 	import codeText._
    	setToolTipText("Enter Logo code here")
        CutCopyPastePopupSupport.enable(codeText)
    }

    val consoleText = new JTextArea();
    {   import consoleText._
    	setPreferredSize(new Dimension(700, 120))
    	setEditable(false)
    	setToolTipText("Console for Logo Parser")
    	CutCopyPastePopupSupport.enable(consoleText)
    }

    def icon(s:String) = new ImageIcon(getClass().getClassLoader().getResource(s))

    lazy val newAction = new AbstractAction {
					override def actionPerformed(ae:ActionEvent) {
						codeText.setText("")
					}
     }

    lazy val openAction = new AbstractAction {
    				override def actionPerformed(ae:ActionEvent) {
    					val chooser = new JFileChooser
    					chooser.setFileFilter(new FileNameExtensionFilter("Logo Files", "logo"))
    					if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
    						val file = chooser.getSelectedFile
    						if (file.exists) {
    							new SwingWorker[StringBuffer,Unit]() {
    								override def doInBackground():StringBuffer = {
    										val reader = new BufferedReader(new FileReader(file))
    										val buffer = new StringBuffer(file.length.asInstanceOf[Int])
    										while (reader.ready) {
    											buffer.append(reader.readLine)
    											buffer.append("\n")
    										}
    										reader.close
    										buffer
    									}

    								override def done() {
    									codeText.setText(get().toString)
    								}
    							}.execute
    						}
    					}
    				}
    			}

    lazy val saveAction = new AbstractAction() {
      		import javax.swing.JOptionPane._

    		override def actionPerformed(ae:ActionEvent) {
    			val chooser = new JFileChooser
    			chooser.setFileFilter(new FileNameExtensionFilter("Logo Files", "logo"))
    			if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
    				val selectedFile = chooser.getSelectedFile
    				val file = if (selectedFile.getName.indexOf(".") >= 0) {
    					chooser.getSelectedFile
    				} else {
    					new File(selectedFile.getAbsolutePath + ".logo")
    				}
    				if (file.exists() &&
    					JOptionPane.showConfirmDialog(frame, "File Exists", "Existing file will be overwritten", OK_CANCEL_OPTION) != OK_OPTION) {
    					return
    				}
  					new SwingWorker[Unit,Unit]() {
  						override def doInBackground() {
  							val writer = new BufferedWriter(new FileWriter(file))
   	 								val text = codeText.getText
   	 								writer.write(text, 0, text.length)
   	 								writer.close
   	 							}
   	 						}.execute
      			}
    	}
    }

    lazy val runAction = new AbstractAction {

  			override def actionPerformed(ae:ActionEvent) {
   				new SwingWorker[LogoParser.ParseResult[List[LogoCommand]],Unit]() {

   					override def doInBackground() = LogoParser.parse(codeText.getText)

   					override def done() {
   						val parseResult = get
   						consoleText.append(parseResult.toString)
   						consoleText.append("\n")
   						canvas.updateParseResult(parseResult)
   					}
   	  			}.execute
   	  		}
     }

	val frame = new JFrame("Scala Logo")
 	val contentPane = frame.getContentPane
	val toolBar = new JToolBar
    toolBar.setFloatable(false)

    {
      import toolBar._

      def createButtonForAction(action:AbstractAction, iconFile:String, toolTipText:String) = {
    	  val btn = new JButton(action)
    	  	btn.setIcon(icon(iconFile))
      		btn.setToolTipText(toolTipText)
      		btn
      }
		add(createButtonForAction(newAction, "new.png", "Start new Logo program"))
    	add(createButtonForAction(openAction, "open.png", "Open existing .logo file"))
    	add(createButtonForAction(saveAction, "save.png", "Save current logo code to file"))
    	add(createButtonForAction(runAction, "go.png", "Run Logo program"))
    }

    val codeCanvasPane = new JSplitPane(
    		JSplitPane.HORIZONTAL_SPLIT,
    		new JScrollPane(codeText),
    		new JScrollPane(canvas)
    )
    codeCanvasPane.setDividerLocation(350)
    codeCanvasPane.setPreferredSize(new Dimension(720, 350))
    contentPane.add(toolBar, BorderLayout.NORTH)
    contentPane.add(new JSplitPane(
    	JSplitPane.VERTICAL_SPLIT,
    		codeCanvasPane,
    		new JScrollPane(consoleText)
    	),BorderLayout.CENTER)

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
    frame.pack
    frame.setVisible(true)
   }


object Main {
  def main(args: Array[String]) {
    new MainApplication
  }
}