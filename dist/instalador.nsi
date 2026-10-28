!include "MUI2.nsh"

!define APP_NAME "IPS Unicatolica de la Gente"
!define MAIN_JAR "Sistema_IPS.jar"

Name "${APP_NAME}"
OutFile "Instalador_IPS_Unicatolica.exe"

; Instala directamente en la carpeta local del usuario para evitar bloqueos de permisos
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
    
    ; Fuerza a empaquetar el JAR especificamente
    File "${MAIN_JAR}"
    
    ; Si existe la carpeta lib la copia opcionalmente, si no existe no falla la compilacion
    File /nonfatal /r "lib"
    
    ; Crea el acceso directo en el Escritorio apuntando a la ruta del usuario
    CreateShortCut "$DESKTOP\${APP_NAME}.lnk" "javaw.exe" '-jar "$INSTDIR\${MAIN_JAR}"' "$INSTDIR"
SectionEnd

Function LanzarApp
    SetOutPath "$INSTDIR"
    Exec '"javaw.exe" -jar "$INSTDIR\${MAIN_JAR}"'
FunctionEnd