package logo

import javax.swing.{AbstractAction, ImageIcon, JComponent, JPanel, JFrame, JSplitPane, JTextArea, JToolBar}
import java.awt.{BorderLayout, Color, Dimension, Graphics, Graphics2D}
import java.awt.event.{ActionEvent}
import java.awt.image.{BufferedImage}

import tfd.gui.swing.CutCopyPastePopupSupport

import scala.util.parsing.combinator.syntactical.StandardTokenParsers

object GraphicUtils {
  
  def doto[T](target: T)(block:T => Any):T = {
        block(target)
        target
  }
}

import GraphicUtils._

abstract class LogoOp
case class Home() extends LogoOp
case class Forward(x: Int) extends LogoOp
case class Turn(x: Int) extends LogoOp
case class Repeat(i: Int, e: List[LogoOp]) extends LogoOp

case class Position(x:Int, y:Int)

class DrawCanvas extends JComponent {
  private val image = new BufferedImage(350, 350, BufferedImage.TYPE_3BYTE_BGR)
  
  private val mySize = new Dimension(350,350)
    			  
  setSize(mySize)
  setMinimumSize(mySize)
  setMaximumSize(mySize)
  
  def doDraw(logoCode: String) {
      val g = image.getGraphics.asInstanceOf[Graphics2D]
	  g.setColor(Color.WHITE)
      g.fillRect(0, 0, 350, 350)
      g.setColor(Color.BLACK)
      g.translate(175, 175)
      Logo.evaluate(logoCode, g);
      repaint()
  }
  
  override def paintComponent(g: Graphics)  = {
    g.drawImage(image, 0, 0, this)
  }
}

class MainApplication {
	var points:List[(Int,Int)] = _
    
    val canvas = new DrawCanvas
    
    val codeText = new JTextArea
    codeText setText """REPEAT 18 [ RT 20 REPEAT 9 [ FD 40 RT 40]]"""
    CutCopyPastePopupSupport.enable(codeText)

    val consoleText = new JTextArea
    consoleText.setPreferredSize(new Dimension(700, 120))
    consoleText.setEditable(false)
    CutCopyPastePopupSupport.enable(consoleText)

	doto(new JFrame()) { $ =>
    	doto($.getContentPane) { $ =>
    	  	$.add(doto (new JToolBar()) { $ =>
    	  	    $.setFloatable(false)
    	  	  	$.add(new AbstractAction("New", new ImageIcon(getClass().getClassLoader().getResource("./new.png"))) {
    	  		  
    	  				def actionPerformed(ae:ActionEvent) {
    	  					
    	  				}
    	  		  
    	  			})  
    	  		$.add(new AbstractAction("Open", new ImageIcon(getClass().getClassLoader().getResource("./open.png"))) {
    	  		  
    	  				def actionPerformed(ae:ActionEvent) {
    	  					
    	  				}
    	  		  
    	  			})  
                $.add(new AbstractAction("Save", new ImageIcon(getClass().getClassLoader().getResource("./save.png"))) {
    	  		  
    	  				def actionPerformed(ae:ActionEvent) {
    	  					
    	  				}
    	  		  
    	  			})  
    	  		$.add(new AbstractAction("Run", new ImageIcon(getClass().getClassLoader().getResource("./go.png"))) {
    	  		  
    	  				def actionPerformed(ae:ActionEvent) {
    	  					System.out.println(LogoParser.parse(codeText.getText))
    	  					canvas.doDraw(codeText.getText)
    	  					canvas.repaint();
    	  				}
    	  		  
    	  			})    	  	  
              }, BorderLayout.NORTH)
    		$.add(
    			new JSplitPane(
    				JSplitPane.VERTICAL_SPLIT,
    					doto(
    						new JSplitPane(
    						JSplitPane.HORIZONTAL_SPLIT,
    						codeText,
    						canvas
    					)) { $ => 
    					  $.setDividerLocation(350)
    					  $.setPreferredSize(new Dimension(720, 350))
    					}, consoleText
    				)
    		      ,BorderLayout.CENTER)
    	}
    	$.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        $.pack
    	$.setVisible(true)
     }
}



object Logo {
  
  implicit def dblToInt(d: Double): Int = 
    if (d  > 0 ) (d +0.5).toInt 
    else (d-0.5).toInt

  def forward(position: Position, x: Int, d: Int) =
	  Position(position.x + x * Math.sin(Math.toRadians(d)), position.y + x * Math.cos(Math.toRadians(d)))
 
  def parse(s: String) : List[LogoOp] = LogoParser.parse(s).get
  
  def evaluate(s : String, g:Graphics2D):Unit = evaluate(parse(s), g, Position(0,0), 0); 

  def evaluate(e : List[LogoOp], g:Graphics2D, position: Position, heading: Int):Unit =  
    e match {
    case Nil => Nil
    case Home() :: tail => evaluate(tail, g, Position(0,0),0)
    case Forward(x) :: tail => {
      val nextPosition = forward(position, x, heading)
      g.drawLine(position.x, position.y, nextPosition.x, nextPosition.y) 
      evaluate(tail, g, nextPosition, heading)
    }
    case Turn(x) :: tail => evaluate(tail, g, position, heading + x)
    case Repeat(0, y) :: tail => evaluate(tail, g, position, heading)
    case Repeat(x, y) :: tail => evaluate(y ::: Repeat(x-1, y)::tail, g, position, heading)
    case _ => Nil
  }
}
   
  object LogoParser extends scala.util.parsing.combinator.JavaTokenParsers  {
    
      
    def program:Parser[List[LogoOp]] = rep(stmt)
    
    def forward = ("FD"|"FORWARD")~>wholeNumber ^^ { case value => Forward(value.toInt) }
    
    def right = ("RT"|"RIGHT")~>wholeNumber ^^ { case value => Turn(value.toInt) }
    
    def left = ("LT"|"LEFT")~>wholeNumber ^^ { case value => Turn(-value.toInt) }
                                               
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