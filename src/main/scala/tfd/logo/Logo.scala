package tfd.logo

import javax.swing.{AbstractAction, ImageIcon, JButton, JComponent, JPanel, JFileChooser, JFrame, JOptionPane, JScrollPane, JSplitPane, JTextArea, JToolBar, SwingWorker}
import javax.swing.filechooser.{FileNameExtensionFilter}
import java.awt.{BorderLayout, Dimension, Graphics, Graphics2D}
import java.awt.event.{ActionEvent}
import java.awt.image.{BufferedImage}
import java.io.{BufferedReader, BufferedWriter, File, FileReader, FileWriter}
import Math._

import tfd.gui.swing.CutCopyPastePopupSupport

import scala.util.parsing.combinator.RegexParsers

sealed abstract class LogoCommand
case class Forward(x: Int) extends LogoCommand
case class Turn(x: Int) extends LogoCommand
case class Repeat(i: Int, e: List[LogoCommand]) extends LogoCommand
case class Color(red:Int, green:Int, blue:Int) extends LogoCommand

class DrawCanvas extends JComponent {
  private var image:BufferedImage = null
  private var imageHeight:Int = 0
  private var imageWidth:Int = 0
  private var parseResult:LogoParser.ParseResult[List[LogoCommand]] = null

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
 
  def updateParseResult(parseResult: LogoParser.ParseResult[List[LogoCommand]]) {
    this.parseResult = parseResult
    doDraw()
  }
    
  def doDraw() {
      val g = image.getGraphics.asInstanceOf[Graphics2D]
	  g.setColor(java.awt.Color.WHITE)
      g.fillRect(0, 0, imageWidth, imageHeight)
      g.setColor(java.awt.Color.BLACK)
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
  class LogoEvaluationState {
	  var x = 0
	  var y = 0
	  var heading = 0
	  var color = java.awt.Color.BLACK
  }
  
  implicit def dblToInt(d: Double):Int = if (d > 0) (d+0.5).toInt else (d-0.5).toInt
 
  def parse(s: String) : List[LogoCommand] = LogoParser.parse(s).get
  
  def evaluate(parseResult: LogoParser.ParseResult[List[LogoCommand]], g:Graphics2D) {
    var state = new LogoEvaluationState
    if (parseResult.successful) {
      evaluate(parseResult.get, g, state)
    }
    // draw turtle
    evaluate(parse("COLOR 0 0 0 RT 90 FD 3 LT 110 FD 10 LT 140 FD 10 LT 110 FD 3"), g, state)
  }
  
//  def evaluate(list : List[LogoCommand], g:Graphics2D, state:LogoEvaluationState) {  
//	  list.foreach(evaluate(_, g, state))
//  }
  
//  def evaluate(command:LogoCommand, g:Graphics2D, state:LogoEvaluationState) {
//	  command match {
//	  	case Forward(distance) => {
//	  		val (nextX, nextY) = (state.x + distance * Math.sin(Math.toRadians(state.heading)),
//                            	  state.y + distance * Math.cos(Math.toRadians(state.heading)))
//            g.drawLine(state.x, state.y, nextX, nextY)
//            state.x = nextX
//            state.y = nextY
//	  	}
//    
//	  	case Turn(degrees) => state.heading += degrees
//    
//	  	case Repeat(count, statements) => (0 to count).foreach { _ =>
//	  										evaluate(statements, g, state)
//	  									  }
//	  }
//   }
  
  private def evaluate(list: List[LogoCommand], g:Graphics2D, state:LogoEvaluationState) {  
	if (!list.isEmpty) {
		val head :: tail = list
		head match {
		  	case Forward(distance) => {
		  		val (nextX, nextY) = (state.x + distance * sin(toRadians(state.heading)),
		  		state.y + distance * cos(toRadians(state.heading)))	
		  		g.setColor(state.color)
      			g.drawLine(state.x, state.y, nextX, nextY)
				state.x = nextX
				state.y = nextY
				evaluate(tail, g, state)
		  	}
		  	case Turn(degrees) => {
		  		state.heading += degrees
		  		evaluate(tail, g, state)
		  	}
		  	case Repeat(0, _) => 	evaluate(tail, g, state)
		  
		  	case Repeat(count, statements) => 
				evaluate(statements ::: Repeat(count-1, statements)::tail, g, state)
    
            case Color(red, green, blue) =>
                state.color = new java.awt.Color(red, green, blue)
                evaluate(tail, g, state)
		}
	}
  }
  
}
   
  object LogoParser extends RegexParsers  {
    
    def nonNegativeInt = """\d+""".r ^^ { _.toInt }
        
    def forward = ("FD"|"FORWARD")~>nonNegativeInt ^^ { Forward(_) }
    
    def right = ("RT"|"RIGHT")~>nonNegativeInt ^^ { value => Turn(-value) }
    
    def left = ("LT"|"LEFT")~>nonNegativeInt ^^ { value => Turn(value) }
                                               
    def repeat = "REPEAT" ~> nonNegativeInt ~ "[" ~ rep(stmt) ~ "]" ^^ { case number~_~stmts~_ => Repeat(number, stmts) }
    
    def color = "COLOR" ~> nonNegativeInt ~ nonNegativeInt ~ nonNegativeInt ^^ { case red~green~blue => Color(red, green, blue) }
        
    def stmt:Parser[LogoCommand] = forward | right | left | repeat | color

    def program = rep(stmt)
    	 
    def parse(text:String) = {
    	parseAll(program, text)
    }    
  }

object Main {
  def main(args: Array[String]) {
    new MainApplication    
  }
}