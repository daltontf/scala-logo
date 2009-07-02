package tfd.logo

import javax.swing.{AbstractAction, ImageIcon, JButton, JComponent, JPanel, JFileChooser, JFrame, JOptionPane, JScrollPane, JSplitPane, JTextArea, JToolBar, SwingWorker}
import javax.swing.filechooser.{FileNameExtensionFilter}
import java.awt.{BorderLayout, Color, Dimension, Graphics, Graphics2D}
import java.awt.event.{ActionEvent}
import java.awt.image.{BufferedImage}
import java.io.{BufferedReader, BufferedWriter, File, FileReader, FileWriter}

import tfd.gui.swing.CutCopyPastePopupSupport

import scala.util.parsing.combinator.JavaTokenParsers

abstract class LogoOp
case class Home() extends LogoOp
case class Forward(x: Int) extends LogoOp
case class Turn(x: Int) extends LogoOp
case class Repeat(i: Int, e: List[LogoOp]) extends LogoOp

case class Position(x:Int, y:Int)

class DrawCanvas extends JComponent {
  private var image:BufferedImage = null
  private var imageHeight:Int = 0
  private var imageWidth:Int = 0
  private var parseResult:LogoParser.ParseResult[List[LogoOp]] = null

  override def setSize(size:Dimension) {
	super.setSize(size)
	if (size.height > 0 && size.width > 0) {
	  imageHeight = size.height
	  imageWidth = size.width
	  if (image != null) image.flush()
   	  image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR)
      if (parseResult != null) {
        doDraw()
      }
	} else {
	  image = null
	}
  }
 
  def updateParseResult(parseResult: LogoParser.ParseResult[List[LogoOp]]) {
    this.parseResult = parseResult
    doDraw()
  }
    
  def doDraw() {
      val g = image.getGraphics.asInstanceOf[Graphics2D]
	  g.setColor(Color.WHITE)
      g.fillRect(0, 0, imageWidth, imageHeight)
      g.setColor(Color.BLACK)
      g.translate(imageWidth/2, imageHeight/2)
      if (parseResult != null) (new Logo).evaluate(parseResult, g)
      repaint()
  }
  
  override def paintComponent(g: Graphics)  = {
    if (image != null) g.drawImage(image, 0, 0, this)
  }
}

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
   				new SwingWorker[LogoParser.ParseResult[List[LogoOp]],Unit]() {
   	  					   	  				    
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
		add(createButtonForAction(newAction, "./new.png", "Start new Logo program"))
    	add(createButtonForAction(openAction, "./open.png", "Open existing .logo file"))
    	add(createButtonForAction(saveAction, "./save.png", "Save current logo code to file"))
    	add(createButtonForAction(runAction, "./go.png", "Run Logo program"))
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

class Logo {
  var heading = 0
  var position = Position(0,0)
  
  implicit def dblToInt(d: Double): Int = 
    if (d  > 0 ) (d +0.5).toInt 
    else (d-0.5).toInt

  def forward(position: Position, x: Int, d: Int) =
	  Position(position.x + x * Math.sin(Math.toRadians(d)), position.y + x * Math.cos(Math.toRadians(d)))
 
  def parse(s: String) : List[LogoOp] = LogoParser.parse(s).get
  
  def evaluate(parseResult: LogoParser.ParseResult[List[LogoOp]], g:Graphics2D) {
    if (parseResult.successful) {
      evaluate(parseResult.get, g)
    }
    // draw turtle
    evaluate(parse("RT 90 FD 3 LT 110 FD 10 LT 140 FD 10 LT 110 FD 3"), g)
  }
  
  def evaluate(e : List[LogoOp], g:Graphics2D):Unit =  
    e match {
    
    case Nil => Nil
    
    case Home() :: tail => {
      heading = 0
      position = Position(0,0)
      evaluate(tail, g)
    }
                            
    case Forward(x) :: tail => {
      val nextPosition = forward(position, x, heading)
      g.drawLine(position.x, position.y, nextPosition.x, nextPosition.y)
      position = nextPosition
      evaluate(tail, g)
    }
    
    case Turn(x) :: tail => {
      heading += x
      evaluate(tail, g)
    }
    
    case Repeat(0, y) :: tail => evaluate(tail, g)
    
    case Repeat(x, y) :: tail => evaluate(y ::: Repeat(x-1, y)::tail, g)
    
    case _ => Nil
  }
}
   
  object LogoParser extends JavaTokenParsers  {
          
    def program:Parser[List[LogoOp]] = rep(stmt)
    
    def forward = ("FD"|"FORWARD")~>wholeNumber ^^ { case value => Forward(value.toInt) }
    
    def right = ("RT"|"RIGHT")~>wholeNumber ^^ { case value => Turn(-value.toInt) }
    
    def left = ("LT"|"LEFT")~>wholeNumber ^^ { case value => Turn(value.toInt) }
                                               
    def repeat = "REPEAT" ~> wholeNumber ~ "[" ~ rep(stmt) ~ "]" ^^ { case number~_~stmts~_ => Repeat(number.toInt, stmts) }
        
    def stmt:Parser[LogoOp] = forward | right | left | repeat
    	 
    def parse(text:String) = {
    	parseAll(program, text)
    }                    
    
  }

object Main {
  def main(args: Array[String]) {
    new MainApplication    
  }
}