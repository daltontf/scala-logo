package tfd.logo

import scala.util.parsing.combinator.RegexParsers

object LogoParser extends RegexParsers  {

    def nonNegativeInt = """\d+""".r ^^ { _.toInt }

    def forward = ("FD"|"FORWARD")~>nonNegativeInt ^^ { Forward(_) }

    def right = ("RT"|"RIGHT")~>nonNegativeInt ^^ { value => Turn(-value) }

    def left = ("LT"|"LEFT")~>nonNegativeInt ^^ { value => Turn(value) }

    def repeat = "REPEAT" ~> nonNegativeInt ~ "[" ~ rep(stmt) ~ "]" ^^ { case number~_~stmts~_ => Repeat(number, stmts) }

    def color = "COLOR" ~> nonNegativeInt ~ nonNegativeInt ~ nonNegativeInt ^^ { case red~green~blue => Color(red, green, blue) }

    def penUp = ("PENUP"|"PU") ^^ { _ => PenUp()}

    def penDown = ("PENDOWN"|"PD") ^^ { _ => PenDown()}

    def stmt:Parser[LogoCommand] = forward | right | left | repeat | color | penUp | penDown

    def program = rep(stmt)

    def parse(text:String) = {
    	parseAll(program, text)
    }
  }