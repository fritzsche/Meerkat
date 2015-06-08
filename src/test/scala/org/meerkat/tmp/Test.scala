package org.meerkat.tmp

import org.meerkat.Syntax._
import Parsers._
import OperatorParsers._
import DefaultLayout._

object Test {
  
  val toStr: String => String = x => x
  val toInt: String => Int = x => x.toInt
  
  object Test1 {
    
    val A = syn { "a" ^ toStr }
    val B = syn { "b" ^ toStr }
    
    val S = syn ( A ~ B  & { (x: (String,String)) => s"${x._1}++${x._2}" } 
                | "c"    ^ { toStr } )
    
    val SStarSep: Nonterminal & List[String] = syn { S.*(",") & { x => x.:+("HoHo!!!") }}                                
    val SStar: Nonterminal & List[String] = syn { S.* & { x => x.:+("HoHo!!!") }}
    val SPlus: Nonterminal & List[String] = syn { S.+ & { x => x.:+("HoHo!!!") }}
    val SOpt: Nonterminal & List[String] = syn { S.? & { x => x.:+("HoHo!!!") }}
    val SGroup: Nonterminal & String = syn { (A ~ B).! & { case (x,y) => x.concat(y) }}
    
    val C = syn { "c" }
    val D = syn { "d" }
    
    val P = syn ( C ~ D | "c" )
    val PStar: Nonterminal = syn { P.** }
    val PPlus: Nonterminal = syn { P.+ } 
    val POpt: Nonterminal = syn { P.? }
    val PGroup: Nonterminal = syn { (C ~ D).! }
    
    val PChar1 = syn { P \ "cdd" }
    val PChar2 = syn { P.* !>> "cd" }
  
    def main(args: Array[String]): Unit = {
      parse("ab,ab,ab", SStarSep)
      // parse("cdcdcd", PStar)
      // parse("ababab", SStar)
      // parse("cdcdcd", PStar)
      // parse("cd", POpt)
      // parse("ab", SOpt)
      // parse("cd cd cd cd", PChar2)
      // parse("c d", PGroup)
//      parse("a b", SGroup)
      // parse("a b", SGroup)
    }
  }
  
  object Test2 {
    
    val C = syn { "c" ^ toStr }
    
    val LIST: Nonterminal & String
      = syn ( LIST ~ C & { case (s1,s2) => s1.concat(s2) } 
            | C )
            
    def main(args: Array[String]): Unit = {
      parse("ccc",C)
    }
  }
  
  object Test3 {
    val E: Nonterminal & Int 
      = syn ( E ~ "+" ~ E & { case (x,y) => x + y }
            | E ~ "*" ~ E & { case (x,y) => x * y }
            | Num         ^ toInt )
  
    val Num = syn { "0" | "1" | "2" | "3" | "4" | "5" }
    
    def main(args: Array[String]): Unit = {
      parse("5    * 3", E)
    }
  }       

  object Test4 {
    val E: Nonterminal 
      = syn ( "(" ~ E ~ ")" 
            | E ~ "*" ~ E 
            | E ~ "/" ~ E
            | E ~ "+" ~ E 
            | E ~ "-" ~ E
            |  "-" ~ E
            |   "a" )
    
    def main(args: Array[String]): Unit = {
      parse("a+a-a*a", E)
    }
  }
  
  object Test5 {
    val E: OperatorNonterminal = syn { E ~ "+" ~ E | "a" }
  }
  
  object Test6 {
    
    val E: OperatorNonterminal & String 
      = syn ( E ~ "+" ~ E & { case (x,y) => x.concat(y) } | "9" ^ toStr )
      
    def main(args: Array[String]): Unit = {
      parse("9+9", E)
    }
  }

  object Test7 {
    
    val E: OperatorNonterminal & Int
      = syn (  "("  ~ E ~ ")"
            |> right { E ~ "*" ~ E } & { case (x,y) => x*y } 
            |> E ~ "+"
            |> left { E ~ "+" ~ E }  & { case (x,y) => x+y }
            |> "-" ~ E               & {x => -x }
            | "3"                    ^ toInt )
    
    def main(args: Array[String]): Unit = {
      // parse("((a))", E)
      parse("3 + - 3 + 3 * 3 + * 3 + 3", E) // 3+(-(3+(((3*3)+)*3)+3)) == -30 !!!
    }
  }
  
  object Test8 {
    
    val E: OperatorNonterminal
      = syn (  "(" ~ E ~ ")" 
            |> right(  E ~ "*" ~ E 
                     | E ~ "/" ~ E )
            |> left(  E ~ "+" ~ E 
                    | E ~ "-" ~ E)
            |> "-" ~ E
            |  "a" )
            
    def main(args: Array[String]): Unit = {
      parse("a+a-a*a/a", E)
    }
  }
  
  object Test9 {
    
    val E: OperatorNonterminal 
      = syn (  "(" ~ E ~ ")" 
            |> right(  E ~ "*" ~ E 
                     | E ~ "/" ~ E )
            |  left(  E ~ "+" ~ E 
                    | E ~ "-" ~ E)
            |> "-" ~ E
            |  "a" )
    
    def main(args: Array[String]): Unit = {
      // parse("a+a-a*a/a", E) // (a+((a-a)*(a/a)))
      parse("a+a-a*a", E)
    }
  }
  
  object Test10 {
    
    val S: Nonterminal
      = syn ( A ~ B.? )
    
    val A: Nonterminal = syn ("a")
    
    val B: Nonterminal = syn ("b")
      
    def main(args: Array[String]): Unit = {
      // parse("a+a-a*a/a", E) // (a+((a-a)*(a/a)))
      parse("ab", S)
    }
  }
  
  object Test11 {
    
    val plus: BinaryOp = new BinaryOp { def apply(x:Int,y:Int) = x + y }
    val times: BinaryOp = new BinaryOp { def apply(x:Int,y:Int) = x * y }
    
    trait BinaryOp extends ((Int,Int) => Int)
    
    val Op: Nonterminal & BinaryOp = syn { "+" ^ { _ => plus } | "*" ^ { _ => times } }
    
    val Num = syn { "0" | "1" | "2" | "3" | "4" | "5" }
    
    // lazy val Es: OperatorNonterminal & List[Int] = E.+(",")
    val E: OperatorNonterminal & Int 
      = syn (  Op ~ "(" ~ E.+(",") ~ ")"   & { case (op,x) => x.reduceLeft((y,z) => op(y,z)) }
            |  left { E ~ "*" ~ E }        & { case (x,y) => x*y }
            |> "-" ~ E                     & { x => -x }
            |> left { E ~ "+" ~ E }        & { case (x,y) => x+y }
            | Num                          ^ toInt )
            
    def main(args: Array[String]): Unit = { 
      val parser: Nonterminal & Int = start(E($))
      parse(" * ( 1 + - 1 + 1 , 1 * - 1 * 1 ) ",parser) // = 0 or -1
    }
  }
  
  object Test12 {
    
    val Num = syn { "[0-9]".r }
    val E: Nonterminal = syn ( E ~~ "*" ~~ E | E ~~ "+" ~~ E | Num )
    
    def main(args: Array[String]): Unit = {
      parse("1+2*3",E)
    }
  }
  
  object Test13 {
    
    val Num = syn { "[0-9]".r }
    val E: Nonterminal = syn ( E ~~ "*" ~~ E | E ~~ "+" ~~ E | Num )
    
    val S = syn ( E.*(",") )
    
    def main(args: Array[String]): Unit = {
      parse("1,2,3",S)
    }
  }
  
   object Test14 {
    
    sealed trait Exp

    case class Add(l: Exp, r: Exp) extends Exp
    case class Mul(l: Exp, r: Exp) extends Exp
    case class Sub(l: Exp, r: Exp) extends Exp
    case class Div(l: Exp, r: Exp) extends Exp
    case class Neg(l: Exp)         extends Exp
    case class Pow(l: Exp, r: Exp) extends Exp
    case class Num(n: Int)         extends Exp
    
    
     val E: OperatorNonterminal & Exp = 
       syn (  right { E ~ "^" ~ E } & { case (x, y) => Pow(x, y) }
           |> "-" ~ E               & { Neg(_) }
           |> left ( E ~ "*" ~ E    & { case (x, y) => Mul(x, y) } 
           |         E ~ "/" ~ E    & { case (x, y) => Div(x, y) }) 
           |> left ( E ~ "+" ~ E    & { case (x, y) => Add(x, y) } 
           |         E ~ "-" ~ E    & { case (x, y) => Sub(x, y) })
           | "(" ~ E ~ ")"
           | "[0-9]".r              ^ { s => Num(toInt(s)) } 
           )
           
     def main(args: Array[String]): Unit = {
       parse("1+2*3-5",E)
     }
     
  }
  
  def main(args: Array[String]): Unit = {
     Test14.main(args)
  }

}
