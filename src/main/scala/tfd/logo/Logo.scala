package logo

import javax.swing.{AbstractAction, ImageIcon, JComponent, JPanel, JFrame, JSplitPane, JTextArea, JToolBar}
import java.awt.{BorderLayout, Color, Dimension, Graphics, Graphics2D}
import java.awt.event.{ActionEvent}
import java.awt.image.{BufferedImage}

import scala.util.parsing.combinator.syntactical.StandardTokenParsers

object GraphicUtils {
  
  def doto[T](target: T)(block:T => Any):T = {
        block(target)
        target
  }
}

import GraphicUtils._


class DrawCanvas extends JComponent {
  private val image = new BufferedImage(350, 350, BufferedImage.TYPE_3BYTE_BGR)
  
  private val mySize = new Dimension(350,350)
    			  
  setSize(mySize)
  setMinimumSize(mySize)
  setMaximumSize(mySize)
  
  def doDraw(points: List[(Int,Int)]) {
	  val g = image.getGraphics.asInstanceOf[Graphics2D]
	  g.setColor(Color.WHITE)
      g.fillRect(0, 0, 350, 350)
      g.setColor(Color.BLACK)
      val mapper = (p:(Int,Int)) => (p._1+size.width/2,-(p._2)+size.height/2)
      var lastPoint : (Int,Int) = mapper (0 ,0)
      for (p <- points.map(mapper)) {
    	if(lastPoint != null) {
    		g.drawLine(lastPoint._1, lastPoint._2,p._1,p._2)
    	}
    	lastPoint = p;
	  }
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
    val consoleText = new JTextArea
    consoleText.setPreferredSize(new Dimension(700, 120))

	doto(new JFrame()) { $ =>
    	doto($.getContentPane) { $ =>
    	  	$.add(doto(new JToolBar()) { $ =>
    	  	  	$.add(new AbstractAction("New", new ImageIcon(getClass().getClassLoader().getResource("./new.png"))) {
    	  		  
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
    	  					canvas.doDraw(Logo.evaluate(codeText.getText))
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

abstract class LogoOp
case class Home() extends LogoOp
case class Forward(x: Int) extends LogoOp
case class Turn(x: Int) extends LogoOp
case class Repeat(i: Int, e: List[LogoOp]) extends LogoOp

object Logo {
  
  implicit def dblToInt(d: Double): Int = 
    if (d  > 0 ) (d +0.5).toInt 
    else (d-0.5).toInt

  def forward(pos: (Int,Int), x: Int, d: Int) : (Int,Int) =
    (pos._1 + x * Math.sin(Math.toRadians(d)), 
      pos._2 + x * Math.cos(Math.toRadians(d)))
 
  def parse(s: String) : List[LogoOp] = LogoParser.parse(s).get
  
  def evaluate(s : String) : List[(Int,Int)]= evaluate(parse(s),(0,0),0); 

  def evaluate(e : List[LogoOp], pos: (Int,Int), heading: Int) 
    : List[(Int,Int)]= e match {
    case Nil => Nil
    case Home() :: _ => (0,0) :: evaluate(e.tail, (0,0),0)
    case Forward(x) :: _ => forward(pos,x,heading) ::
            evaluate(e.tail, forward(pos,x,heading), heading)
    case Turn(x) :: _ => evaluate(e.tail, pos, heading + x)
    case Repeat(0, y) :: _ => evaluate(e.tail, pos, heading)
    case Repeat(x, y) :: tail => evaluate(y ::: Repeat(x-1, y)::e.tail,pos, heading)
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