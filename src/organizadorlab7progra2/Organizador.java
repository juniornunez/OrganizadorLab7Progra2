/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package organizadorlab7progra2;

/**
 *
 * @author Junior Nuñes
 */
import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class Organizador extends JFrame {

    private JTree treeArchivos;
    private DefaultMutableTreeNode nodoSeleccionado;
    private File directorioRaiz;
    private File archivoCopiado = null;
    private boolean cortar = false;

    public Organizador(File directorioRaiz) {
        this.directorioRaiz = directorioRaiz;
        setTitle("Navegador de Archivos");
        setSize(600, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        DefaultMutableTreeNode raiz = crearNodoArbol(directorioRaiz);
        treeArchivos = new JTree(raiz);
        JScrollPane scrollPane = new JScrollPane(treeArchivos);
        add(scrollPane, BorderLayout.CENTER);

        JPopupMenu menu = new JPopupMenu();
        JMenuItem crearArchivo = new JMenuItem("Crear Archivo");
        JMenuItem crearCarpeta = new JMenuItem("Crear Carpeta");
        JMenuItem delete = new JMenuItem("Eliminar");
        JMenuItem renombrar = new JMenuItem("Renombrar");
        JMenuItem escribirDatos = new JMenuItem("Registrar Datos");
        JMenuItem organizar = new JMenuItem("Organizar Archivos");
        JMenuItem organizadorCarpetas = new JMenuItem("Organizar por Carpetas");
        JMenuItem copiar = new JMenuItem("Copiar");
        JMenuItem pegar = new JMenuItem("Pegar");

        menu.add(crearArchivo);
        menu.add(crearCarpeta);
        menu.add(delete);
        menu.add(renombrar);
        menu.add(escribirDatos);
        menu.add(organizar);
        menu.add(organizadorCarpetas);
        menu.add(copiar);
        menu.add(pegar);

        treeArchivos.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = treeArchivos.getRowForLocation(e.getX(), e.getY());
                if (selRow != -1 && SwingUtilities.isRightMouseButton(e)) {
                    TreePath selPath = treeArchivos.getPathForLocation(e.getX(), e.getY());
                    nodoSeleccionado = (DefaultMutableTreeNode) selPath.getLastPathComponent();
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
        });

        crearArchivo.addActionListener(e -> {
            File directorio = directorioSeleccionado();
            if (directorio == null || !directorio.exists() || !directorio.isDirectory()) {
                JOptionPane.showMessageDialog(this, "La ruta especificada no existe o no es un directorio.");
                return;
            }

            String nombreArchivo = JOptionPane.showInputDialog("Nombre del nuevo archivo:");
            if (nombreArchivo != null && !nombreArchivo.trim().isEmpty()) {
                File archivo = new File(directorio, nombreArchivo);
                try {
                    if (archivo.createNewFile()) {
                        JOptionPane.showMessageDialog(this, "Archivo creado: " + archivo.getName());
                        recargar();
                    } else {
                        JOptionPane.showMessageDialog(this, "No se pudo crear el archivo. Verifique el nombre o permisos.");
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Error al crear el archivo: " + ex.getMessage());
                }
            } else {
                JOptionPane.showMessageDialog(this, "El nombre del archivo no puede estar vacio.");
            }
        });

        crearCarpeta.addActionListener(e -> {
            File directorio = directorioSeleccionado();
            if (directorio == null || !directorio.exists() || !directorio.isDirectory()) {
                JOptionPane.showMessageDialog(this, "La ruta especificada no existe o no es un directorio");
                return;
            }

            String nombreCarpeta = JOptionPane.showInputDialog("Nombre de la nueva carpeta:");
            if (nombreCarpeta != null && !nombreCarpeta.trim().isEmpty()) {
                File carpeta = new File(directorio, nombreCarpeta);
                if (carpeta.mkdirs()) {
                    JOptionPane.showMessageDialog(this, "Carpeta creada: " + carpeta.getName());
                    recargar();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo crear la carpeta");
                }
            } else {
                JOptionPane.showMessageDialog(this, "El nombre de la carpeta no puede estar vacio");
            }
        });

        delete.addActionListener(e -> {
            File archivo = directorioSeleccionado();
            if (archivo != null && archivo.exists()) {
                int confirmacion = JOptionPane.showConfirmDialog(this, "Estas seguro de que deseas eliminar " + archivo.getName() + "?", "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
                if (confirmacion == JOptionPane.YES_OPTION) {
                    try {
                        eliminarRecursivo(archivo);
                        JOptionPane.showMessageDialog(this, "Archivo/Carpeta eliminado");
                        recargar();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error al eliminar archivo/carpeta: " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "El archivo/carpeta no existe");
            }
        });

        renombrar.addActionListener(e -> {
            File archivo = directorioSeleccionado();
            if (archivo == null || !archivo.exists()) {
                JOptionPane.showMessageDialog(this, "El archivo/carpeta no existe");
                return;
            }

            String nuevoNombre = JOptionPane.showInputDialog("Nuevo nombre:");
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                File nuevoArchivo = new File(archivo.getParent(), nuevoNombre);
                if (archivo.renameTo(nuevoArchivo)) {
                    JOptionPane.showMessageDialog(this, "Archivo renombrado a: " + nuevoArchivo.getName());
                    recargar();
                } else {
                    JOptionPane.showMessageDialog(this, "No se pudo renombrar el archivo/carpeta.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "El nuevo nombre no puede estar vacio");
            }
        });

        escribirDatos.addActionListener(e -> {
            File archivo = directorioSeleccionado();
            if (archivo != null && archivo.isFile()) {
                String texto = JOptionPane.showInputDialog("Escribe el texto a registrar:");
                if (texto != null && !texto.trim().isEmpty()) {
                    try (FileWriter writer = new FileWriter(archivo, true)) {
                        writer.write(texto + "\n");
                        JOptionPane.showMessageDialog(this, "Datos registrados");
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(this, "Error al escribir en el archivo: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "El texto no puede estar vacio");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Selecciona un archivo valido para escribir");
            }
        });

        copiar.addActionListener(e -> {
            File seleccionado = directorioSeleccionado();
            if (seleccionado != null && seleccionado.exists()) {
                archivoCopiado = seleccionado;
                cortar = false;
                JOptionPane.showMessageDialog(this, "Archivo/Carpeta copiado: " + archivoCopiado.getName());
            } else {
                JOptionPane.showMessageDialog(this, "No se selecciono ningun archivo o carpeta.");
            }
        });

        pegar.addActionListener(e -> {
            if (archivoCopiado == null) {
                JOptionPane.showMessageDialog(this, "No hay ningun archivo/carpeta para pegar.");
                return;
            }

            File directorioDestino = directorioSeleccionado();
            if (directorioDestino == null || !directorioDestino.isDirectory()) {
                JOptionPane.showMessageDialog(this, "Selecciona un directorio valido para pegar.");
                return;
            }

            File archivoPegado = new File(directorioDestino, archivoCopiado.getName());
            if (archivoPegado.exists()) {
                archivoPegado = new File(directorioDestino, generarNombreUnico(archivoPegado));
            }

            try {
                if (archivoCopiado.isDirectory()) {
                    copiarDirectorio(archivoCopiado.toPath(), archivoPegado.toPath());
                } else {
                    Files.copy(archivoCopiado.toPath(), archivoPegado.toPath());
                }
                JOptionPane.showMessageDialog(this, "Archivo/Carpeta pegado: " + archivoPegado.getName());
                recargar();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al pegar el archivo/carpeta: " + ex.getMessage());
            }
        });

        organizar.addActionListener(e -> {
            String[] opciones = {"Nombre", "Fecha", "Tipo", "Tamaño"};
            String criterio = (String) JOptionPane.showInputDialog(this, "Seleccion de que forma quiere oganizar:", "Organizar Archivos", JOptionPane.PLAIN_MESSAGE, null, opciones, opciones[0]);
            if (criterio != null) {
                organizarArchivos(criterio);
            }
        });

        organizadorCarpetas.addActionListener(e -> organizarPorCarpetas());
    }

    private void recargar() {
        DefaultMutableTreeNode raiz = crearNodoArbol(directorioRaiz);
        treeArchivos.setModel(new DefaultTreeModel(raiz));
    }

    private void eliminarRecursivo(File archivo) throws IOException {
        if (archivo.isDirectory()) {
            File[] archivos = archivo.listFiles();
            if (archivos != null) {
                for (File subArchivo : archivos) {
                    eliminarRecursivo(subArchivo);
                }
            }
        }
        if (!archivo.delete()) {
            throw new IOException("No se pudo eliminar: " + archivo.getAbsolutePath());
        }
    }

    private File directorioSeleccionado() {
        if (nodoSeleccionado == null) {
            return null;
        }

        StringBuilder ruta = new StringBuilder(directorioRaiz.getAbsolutePath());
        TreeNode[] pathNodes = nodoSeleccionado.getPath();
        for (int i = 1; i < pathNodes.length; i++) {
            ruta.append(File.separator).append(pathNodes[i].toString());
        }

        return new File(ruta.toString());
    }

    private DefaultMutableTreeNode crearNodoArbol(File archivo) {
        DefaultMutableTreeNode nodo = new DefaultMutableTreeNode(archivo.getName());
        if (archivo.isDirectory()) {
            File[] archivos = archivo.listFiles();
            if (archivos != null) {
                Arrays.sort(archivos, Comparator.comparing(File::getName));
                for (File subArchivo : archivos) {
                    nodo.add(crearNodoArbol(subArchivo));
                }
            }
        }
        return nodo;
    }

    private String generarNombreUnico(File archivo) {
        String nombre = archivo.getName();
        String extension = "";
        int i = nombre.lastIndexOf('.');
        if (i > 0) {
            extension = nombre.substring(i);
            nombre = nombre.substring(0, i);
        }
        int contador = 1;
        while (archivo.exists()) {
            archivo = new File(archivo.getParent(), nombre + " (" + contador + ")" + extension);
            contador++;
        }
        return archivo.getName();
    }

    private void copiarDirectorio(Path origen, Path destino) throws IOException {
        Files.walk(origen).forEach(source -> {
            try {
                Path destinoRuta = destino.resolve(origen.relativize(source));
                if (Files.isDirectory(source)) {
                    Files.createDirectories(destinoRuta);
                } else {
                    Files.copy(source, destinoRuta, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error al copiar directorio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void organizarArchivos(String criterio) {
        File dir = directorioSeleccionado();
        if (dir != null && dir.isDirectory()) {
            File[] archivos = dir.listFiles();
            if (archivos != null) {
                Comparator<File> comparator = null;

                switch (criterio) {
                    case "Nombre":
                        comparator = Comparator.comparing(File::isFile).thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER);
                        break;
                    case "Fecha":
                        comparator = Comparator.comparingLong(File::lastModified).reversed().thenComparing(File::isFile);
                        break;
                    case "Tipo":
                        comparator = Comparator.comparing((File f) -> f.isDirectory() ? "" : getFileExtension(f)).thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER);
                        break;
                    case "Tamaño":
                        comparator = Comparator.comparingLong(File::length).reversed().thenComparing(File::isFile).thenComparing(File::getName, String.CASE_INSENSITIVE_ORDER);
                        break;
                    default:
                        JOptionPane.showMessageDialog(this, "Criterio no valido.");
                        return;
                }

                Arrays.sort(archivos, comparator);

                nodoSeleccionado.removeAllChildren();
                for (File archivo : archivos) {
                    nodoSeleccionado.add(crearNodoArbol(archivo));
                }

                ((DefaultTreeModel) treeArchivos.getModel()).nodeStructureChanged(nodoSeleccionado);
            }
        }
    }

    private String obtenerCategoria(String criterio, File archivo) {
        switch (criterio) {
            case "Nombre":
                return getFileNameCategory(archivo);
            case "Fecha":
                return getFileDateCategory(archivo);
            case "Tipo":
                return getFileExtension(archivo).toUpperCase();
            case "Tamaño":
                return getFileSizeCategory(archivo);
            default:
                return "Otros";
        }
    }

    private String getFileNameCategory(File archivo) {
        return archivo.getName().substring(0, 1).toUpperCase();
    }

    private String getFileDateCategory(File archivo) {
        SimpleDateFormat formatofecha = new SimpleDateFormat("yyyy");
        return formatofecha.format(new Date(archivo.lastModified()));
    }

    private String getFileSizeCategory(File archivo) {
        long tamaño = archivo.length();
        if (tamaño < 1_000_000) {
            return "Menos de 1 MB";
        }
        if (tamaño < 10_000_000) {
            return "1 MB - 10 MB";
        }
        return "Mas de 10 MB";
    }

    private void organizarPorCarpetas() {
        File dir = directorioSeleccionado();
        if (dir != null && dir.isDirectory()) {
            File[] archivos = dir.listFiles();
            if (archivos != null) {
                for (File archivo : archivos) {
                    if (archivo.isFile()) {
                        String extension = getFileExtension(archivo);
                        File nuevaCarpeta = new File(dir, extension.toUpperCase());
                        if (!nuevaCarpeta.exists()) {
                            nuevaCarpeta.mkdir();
                        }
                        archivo.renameTo(new File(nuevaCarpeta, archivo.getName()));
                    }
                }
                recargar();
            }
        }
    }

    private String getFileExtension(File archivo) {
        String nombre = archivo.getName();
        int lastIndex = nombre.lastIndexOf('.');
        if (lastIndex > 0 && lastIndex < nombre.length() - 1) {
            return nombre.substring(lastIndex + 1);
        }
        return "Otros";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFileChooser selectorDirectorio = new JFileChooser();
            selectorDirectorio.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int seleccion = selectorDirectorio.showOpenDialog(null);
            if (seleccion == JFileChooser.APPROVE_OPTION) {
                File raiz = selectorDirectorio.getSelectedFile();
                Organizador ventana = new Organizador(raiz);
                ventana.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "No seleccionaste nada, saliendo del programa");
            }
        });
    }
}