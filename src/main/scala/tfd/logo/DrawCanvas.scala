package tfd.logo

import java.awt.image.BufferedImage
import java.awt.{Dimension, Graphics2D, Graphics}
import javax.swing.JComponent

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
      if (parseResult != null) (new LogoEvaluator).evaluate(parseResult, g)
      repaint()
  }

  override def paintComponent(g: Graphics)  = {
    if (image != null) g.drawImage(image, 0, 0, this)
  }
}