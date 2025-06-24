import base64
import os

def generate_jwt_secret():
    """
    Genera una clave secreta segura para JWT (Base64 URL-safe).
    Se generan 32 bytes aleatorios (256 bits), adecuados para algoritmos como HS256.
    """
    try:
        # Genera 32 bytes aleatorios
        random_bytes = os.urandom(32)

        # Codifica los bytes en Base64 URL-safe
        # .decode('utf-8') convierte los bytes resultantes a una cadena de texto
        # .rstrip('=') elimina cualquier carácter de relleno '=' al final,
        # lo cual es común y a menudo preferido en JWTs para ser más compactos.
        jwt_secret_base64 = base64.urlsafe_b64encode(random_bytes).decode('utf-8').rstrip('=')

        return jwt_secret_base64
    except Exception as e:
        print(f"Ocurrió un error al generar la clave: {e}")
        return None

if __name__ == "__main__":
    secret_key = generate_jwt_secret()
    if secret_key:
        print("¡Clave JWT secreta generada con éxito!")
        print("Copia esta clave y pégala en tu archivo application.properties:")
        print(f"\n{secret_key}\n")
        print("Asegúrate de reemplazar el valor de 'yuki.academy.jwtSecret'.")