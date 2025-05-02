package utils

import cats.Alternative
import cats.effect.IO

object AlternativeOps:
  // Alternative instance for IO, similar to the one in Haskell
  given Alternative[IO] with
    def pure[A](x: A): IO[A] = IO.pure(x)
    def empty[A]: IO[A] = IO.raiseError(Throwable())
    def ap[A, B](ff: IO[A => B])(fa: IO[A]): IO[B] = ff.flatMap(f => fa.map(f(_)))
    def combineK[A](x: IO[A], y: IO[A]): IO[A] = x.handleErrorWith(_ => y)

  extension[A, F[_] : Alternative](fa: F[A])
    infix def <|>(fb: F[A]) = Alternative[F].combineK(fa, fb)