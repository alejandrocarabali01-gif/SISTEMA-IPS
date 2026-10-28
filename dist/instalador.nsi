!include "MUI2.nsh"

!define APP_NAME "IPS Unicatolica de la Gente"
!define MAIN_JAR "Sistema_IPS.jar"

Name "${APP_NAME}"
OutFile "Instalador_IPS_Unicatolica.exe"

; Instala directamente en la carpeta local del usuario para evitar requerir permisos de administrador
InstallDir "$LOCALAPPDATA\IPS Unicatolica"

!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_INSTFILES

!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_FUNCTION "LanzarApp"
!define MUI_FINISHPAGE_RUN_TEXT "Ejecutar ${APP_NAME} ahora"
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_LANGUAGE "Spanish"

Section "Instalar Aplicacion"
    SetOutPath "$INSTDIR"
    
    ; Empaquetar el ejecutable JAR
    File "${MAIN_JAR}"
    
    ; Empaquetar el archivo de configuración externa
    File /nonfatal "config.properties"
    
    ; Empaquetar la carpeta de librerías (Connector JDBC)
    File /nonfatal /r "lib"
    
    ; Crear el acceso directo en el Escritorio
    CreateShortCut "$DESKTOP\${APP_NAME}.lnk" "javaw.exe" '-jar "$INSTDIR\${MAIN_JAR}"' "$INSTDIR"
SectionEnd

Function LanzarApp
    SetOutPath "$INSTDIR"
    Exec '"javaw.exe" -jar "$INSTDIR\${MAIN_JAR}"'
FunctionEnd