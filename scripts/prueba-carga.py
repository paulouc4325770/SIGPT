# -*- coding: utf-8 -*-
"""Prueba de carga del SIGPT (RNF01: las consultas de planta deben responder
en menos de 2 segundos).

Simula el uso simultaneo de la planta: varios operarios y supervisores
consultan y registran avance al mismo tiempo, y el Jefe de Produccion consulta
la bandeja de pedidos.

Uso:
    python scripts/prueba-carga.py --usuarios 20 --iteraciones 10
    python scripts/prueba-carga.py --url http://localhost:8080 --usuarios 10
"""
import argparse
import http.cookiejar
import statistics
import threading
import time
import urllib.error
import urllib.parse
import urllib.request

CLAVE = "Sigpt2026*"

# Escenarios: cada pantalla se consulta con el perfil que tiene permiso
# (Matriz de Accesos del Anexo 2 de la especificacion).
RUTAS = [
    ("supervisor", "/inicio"),
    ("supervisor", "/planta/avance"),
    ("supervisor", "/planta/asignacion"),
    ("supervisor", "/planta/calidad"),
    ("jperez", "/pedidos/bandeja"),
    ("jperez", "/reportes/avance-pedidos"),
]


class PoliticaPermisiva(http.cookiejar.DefaultCookiePolicy):
    """Permite enviar la cookie de sesion aunque el servidor la marque HttpOnly.

    Python la omite por defecto, y el contenedor terminaba reescribiendo la URL
    con ;jsessionid, que es justo lo que un usuario con cookies deshabilitadas
    haria en un navegador.
    """

    def return_ok_httponly(self, cookie, request):
        return True


class Worker(threading.Thread):
    """Un usuario simulado que recorre las pantallas y mide los tiempos."""

    def __init__(self, url, usuario, iteraciones, resultados, bloqueo):
        super().__init__(daemon=True)
        self.url = url.rstrip("/")
        self.usuario = usuario
        self.iteraciones = iteraciones
        self.resultados = resultados
        self.bloqueo = bloqueo

    def sesion(self, usuario):
        jar = http.cookiejar.CookieJar(policy=PoliticaPermisiva())
        opener = urllib.request.build_opener(
            urllib.request.HTTPCookieProcessor(jar))
        cuerpo = urllib.parse.urlencode(
            {"codUsuario": usuario, "clave": CLAVE}).encode("utf-8")
        peticion = urllib.request.Request(self.url + "/seguridad/login", data=cuerpo)
        with opener.open(peticion, timeout=30) as respuesta:
            respuesta.read()
        return opener

    def run(self):
        sesiones = {}
        for usuario in sorted({u for u, _ in RUTAS}):
            try:
                sesiones[usuario] = self.sesion(usuario)
            except Exception as e:  # noqa: BLE001
                with self.bloqueo:
                    self.resultados.append(("LOGIN", usuario, 0, 0, str(e)))
                return
        for _ in range(self.iteraciones):
            for usuario, ruta in RUTAS:
                opener = sesiones[usuario]
                inicio = time.perf_counter()
                estado = 0
                error = ""
                try:
                    with opener.open(self.url + ruta, timeout=30) as respuesta:
                        respuesta.read()
                        estado = respuesta.status
                except urllib.error.HTTPError as e:
                    estado = e.code
                    error = str(e)
                except Exception as e:  # noqa: BLE001
                    error = str(e)
                duracion = (time.perf_counter() - inicio) * 1000
                with self.bloqueo:
                    self.resultados.append((ruta, usuario, estado, duracion, error))


def main():
    analisador = argparse.ArgumentParser(description="Prueba de carga del SIGPT")
    analisador.add_argument("--url", default="http://localhost:8080")
    analisador.add_argument("--usuarios", type=int, default=20,
                            help="usuarios concurrentes (hilos)")
    analisador.add_argument("--iteraciones", type=int, default=5,
                            help="vueltas de cada usuario sobre las pantallas")
    argumentos = analisador.parse_args()

    print("Prueba de carga del SIGPT")
    print("  url:         " + argumentos.url)
    print("  usuarios:    " + str(argumentos.usuarios) + " concurrentes")
    print("  iteraciones: " + str(argumentos.iteraciones) + " por usuario")
    print("  pantallas:   " + ", ".join(ruta for _, ruta in RUTAS))
    print()

    resultados = []
    bloqueo = threading.Lock()
    hilos = [Worker(argumentos.url, "supervisor", argumentos.iteraciones, resultados, bloqueo)
             for _ in range(argumentos.usuarios)]

    inicioTotal = time.perf_counter()
    for hilo in hilos:
        hilo.start()
    for hilo in hilos:
        hilo.join()
    duracionTotal = time.perf_counter() - inicioTotal

    if not resultados:
        print("No se pudo ejecutar la prueba: la aplicacion no respondio.")
        return 1

    porRuta = {}
    for ruta, usuario, estado, duracion, error in resultados:
        porRuta.setdefault(ruta, []).append((estado, duracion, error))

    print("%-28s %6s %8s %8s %8s %8s" % ("pantalla", "peticiones", "p50 (ms)", "p95 (ms)",
                                         "max (ms)", "errores"))
    print("-" * 72)
    limite = 2000
    fueraDeNorma = 0
    for ruta, valores in porRuta.items():
        tiempos = sorted(t for _, t, _ in valores)
        errores = sum(1 for e, _, _ in valores if e != 200)
        p50 = statistics.median(tiempos)
        p95 = tiempos[min(len(tiempos) - 1, int(len(tiempos) * 0.95))]
        if p95 > limite:
            fueraDeNorma += 1
        print("%-28s %6d %8.0f %8.0f %8.0f %8d" % (ruta, len(valores), p50, p95,
                                                   tiempos[-1], errores))

    todos = [t for _, _, t, _ in [(r, u, e, d) for r, u, e, d, _ in resultados]]
    todosOrdenados = sorted(todos)
    p95General = todosOrdenados[min(len(todosOrdenados) - 1,
                                     int(len(todosOrdenados) * 0.95))]
    print("-" * 72)
    print("  peticiones totales: %d en %.1f s (%.0f peticiones/s)"
          % (len(resultados), duracionTotal, len(resultados) / duracionTotal))
    print("  p95 general:        %.0f ms (limite RNF01: %d ms)" % (p95General, limite))
    print("  pantallas fuera de norma: %d" % fueraDeNorma)
    print()
    if fueraDeNorma == 0:
        print("RESULTADO: se cumple el RNF01 (tiempo de respuesta menor a 2 segundos).")
    else:
        print("RESULTADO: hay pantallas por encima del limite de 2 segundos.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
