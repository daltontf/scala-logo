package tfd.logo

import java.awt.Graphics2D
import java.lang.Math._

class LogoEvaluator {
  class LogoEvaluationState {
	  var x = 0
	  var y = 0
	  var heading = 0
	  var color = java.awt.Color.BLACK
      var penup = false
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
		  		if (!state.penup) {
		  			g.drawLine(state.x, state.y, nextX, nextY)
		  		}
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

            case Color(red, green, blue) => {
                state.color = new java.awt.Color(red, green, blue)
                evaluate(tail, g, state)
            }
            case PenDown() => {
            	state.penup = false
            	evaluate(tail, g, state)
            }
            case PenUp() => {
            	state.penup = true
            	evaluate(tail, g, state)
            }
		}
	}
  }

}
