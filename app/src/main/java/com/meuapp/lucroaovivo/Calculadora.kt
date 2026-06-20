
package com.meuapp.lucroaovivo
object Calculadora{
 fun calcularLucro(preco:Double,distancia:Double,custoKm:Double)=preco-(distancia*custoKm)
 fun calcularPorMin(lucro:Double,tempoMin:Double)=if(tempoMin>0) lucro/tempoMin else 0.0
 fun calcularPorKm(lucro:Double,distancia:Double)=if(distancia>0) lucro/distancia else 0.0
}
