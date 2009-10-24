package tfd.logo

sealed abstract class LogoCommand
case class Forward(x: Int) extends LogoCommand
case class Turn(x: Int) extends LogoCommand
case class Repeat(i: Int, e: List[LogoCommand]) extends LogoCommand
case class Color(red:Int, green:Int, blue:Int) extends LogoCommand
case class PenUp() extends LogoCommand
case class PenDown() extends LogoCommand