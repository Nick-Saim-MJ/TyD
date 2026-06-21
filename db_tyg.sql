-- phpMyAdmin SQL Dump
-- version 5.2.0
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Jun 18, 2026 at 11:01 PM
-- Server version: 8.0.30
-- PHP Version: 8.3.7

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `db_tyg`
--

-- --------------------------------------------------------

--
-- Table structure for table `activaciones`
--

CREATE TABLE `activaciones` (
  `id` bigint NOT NULL,
  `venta_id` bigint NOT NULL,
  `monto_recarga_inicial` decimal(10,2) DEFAULT NULL,
  `estado` enum('ACTIVO','PENDIENTE') NOT NULL DEFAULT 'PENDIENTE',
  `fecha_activacion` timestamp NULL DEFAULT NULL COMMENT 'NULL mientras estado=PENDIENTE (caso frecuente con freelance)',
  `registrado_por_id` bigint DEFAULT NULL COMMENT 'Puede ser diferente al vendedor (activacion diferida freelance)',
  `comentarios` varchar(500) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Activacion DirecTV. Se crea en PENDIENTE y se actualiza a ACTIVO al confirmar. Soporta activacion diferida para freelance';

--
-- Dumping data for table `activaciones`
--

INSERT INTO `activaciones` (`id`, `venta_id`, `monto_recarga_inicial`, `estado`, `fecha_activacion`, `registrado_por_id`, `comentarios`, `created_at`) VALUES
(1, 1, '35.00', 'ACTIVO', '2025-01-20 16:00:00', 6, 'Activacion inmediata. Cliente recargo S/35 en el acto', '2026-05-04 20:42:06'),
(2, 2, '35.00', 'ACTIVO', '2025-01-22 20:30:00', 4, 'Cliente Brayan Condori, activado en tienda', '2026-05-04 20:42:06'),
(3, 3, '70.00', 'ACTIVO', '2026-05-13 00:55:06', 1, 'Se completo el primer Pago', '2026-05-04 20:42:06'),
(4, 4, '35.00', 'ACTIVO', '2025-01-24 14:30:00', 8, 'Activacion en sede Cusco Centro', '2026-05-04 20:42:06'),
(5, 5, '70.00', 'ACTIVO', '2025-01-26 20:00:00', 8, 'Cliente pago 2 meses de recarga por adelantado', '2026-05-04 20:42:06'),
(6, 6, NULL, 'PENDIENTE', NULL, NULL, 'Vendedor freelance. Activacion diferida, pendiente confirmacion DirecTV', '2026-05-04 20:42:06'),
(7, 7, '35.00', 'ACTIVO', '2025-02-10 15:30:00', 5, 'Activado inmediatamente en Oficina 2 Juliaca', '2026-05-04 20:42:06'),
(8, 8, '35.00', 'ACTIVO', '2025-01-28 17:30:00', 6, NULL, '2026-05-04 20:42:06'),
(9, 9, '10.00', 'PENDIENTE', NULL, NULL, 'Activación cancelada por anulación de venta. Motivo: KitDefectuoso', '2026-05-13 02:13:56'),
(10, 10, '30.00', 'PENDIENTE', NULL, NULL, NULL, '2026-05-31 06:36:45');

-- --------------------------------------------------------

--
-- Table structure for table `audit_log`
--

CREATE TABLE `audit_log` (
  `id` bigint NOT NULL,
  `tabla_nombre` varchar(100) NOT NULL,
  `registro_id` bigint NOT NULL,
  `accion` enum('INSERT','UPDATE','DELETE') NOT NULL,
  `datos_anteriores` json DEFAULT NULL COMMENT 'Snapshot del registro ANTES del cambio (para UPDATE y DELETE)',
  `datos_nuevos` json DEFAULT NULL COMMENT 'Snapshot del registro DESPUES del cambio (para INSERT y UPDATE)',
  `usuario_id` bigint DEFAULT NULL COMMENT 'NULL si fue un proceso automatico del sistema',
  `ip_address` varchar(45) DEFAULT NULL COMMENT 'Soporta IPv6 (45 chars)',
  `user_agent` varchar(500) DEFAULT NULL,
  `fecha` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Log de auditoria INMUTABLE. NUNCA ejecutar UPDATE ni DELETE sobre esta tabla. Se puebla desde Spring AOP (@AfterReturning en servicios anotados con @Auditable)';

--
-- Dumping data for table `audit_log`
--

INSERT INTO `audit_log` (`id`, `tabla_nombre`, `registro_id`, `accion`, `datos_anteriores`, `datos_nuevos`, `usuario_id`, `ip_address`, `user_agent`, `fecha`) VALUES
(1, 'items_kit', 1, 'UPDATE', '{\"estado\": \"DISPONIBLE\", \"custodio_actual_id\": 6}', '{\"estado\": \"VENDIDO\", \"custodio_actual_id\": null}', 6, '192.168.1.45', NULL, '2025-01-20 15:30:00'),
(2, 'ventas', 1, 'INSERT', NULL, '{\"condicion\": \"CONTADO\", \"cliente_id\": 1, \"item_kit_id\": 1, \"monto_venta\": 110.0, \"vendedor_id\": 6}', 6, '192.168.1.45', NULL, '2025-01-20 15:30:00'),
(3, 'liquidaciones_caja', 2, 'UPDATE', '{\"estado\": \"PENDIENTE\", \"monto_depositado\": null}', '{\"estado\": \"OBSERVADO\", \"monto_depositado\": 240.0}', 1, '192.168.0.10', NULL, '2025-02-01 14:30:00'),
(4, 'usuarios', 10, 'INSERT', NULL, '{\"rol\": \"VENDEDOR\", \"username\": \"freelance.rm\", \"sucursal_id\": null}', 1, '192.168.0.10', NULL, '2025-02-10 13:00:00'),
(5, 'kardex_mensual', 1, 'UPDATE', '{\"cerrado\": false}', '{\"cerrado\": true}', 1, '192.168.0.10', NULL, '2025-02-02 15:00:00'),
(6, 'sucursales', -1, 'INSERT', NULL, '{\"id\": 11, \"tipo\": \"ALMACEN\", \"activo\": true, \"nombre\": \"Sucursal Central TyG\", \"ubigeo\": \"150101\", \"zonaId\": 1, \"direccion\": \"Av. Los Angeles 123\", \"zonaNombre\": \"Puno\", \"ubicacionFisicaId\": null, \"ubicacionFisicaNombre\": null}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-07 03:11:32'),
(7, 'usuarios', -1, 'INSERT', NULL, '{\"id\": 11, \"rol\": \"ADMIN\", \"email\": \"admin@empresa.com\", \"activo\": true, \"zonaId\": null, \"username\": \"admin.nacional\", \"createdAt\": \"2026-05-06T22:17:49.172941Z\", \"sucursalId\": null, \"zonaNombre\": null, \"ultimoLogin\": null, \"nombreCompleto\": \"Administrador General\", \"sucursalNombre\": null}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-07 03:17:49'),
(8, 'usuarios', -1, 'UPDATE', NULL, '{\"id\": 11, \"rol\": \"ADMIN\", \"email\": \"admin@empresa.com\", \"activo\": true, \"zonaId\": null, \"username\": \"admin.nacional\", \"createdAt\": \"2026-05-06T22:17:49Z\", \"sucursalId\": null, \"zonaNombre\": null, \"ultimoLogin\": null, \"nombreCompleto\": \"Cuenta Limitada\", \"sucursalNombre\": null}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-07 03:18:43'),
(9, 'usuarios', -1, 'DELETE', NULL, '{\"id\": 11, \"rol\": \"VENDEDOR\", \"email\": \"admin@empresa.com\", \"activo\": false, \"zonaId\": 1, \"username\": \"admin.nacional\", \"createdAt\": \"2026-05-06T22:17:49Z\", \"sucursalId\": null, \"zonaNombre\": \"Puno\", \"ultimoLogin\": null, \"nombreCompleto\": \"Cuenta Limitada\", \"sucursalNombre\": null}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-07 03:27:56'),
(10, 'zonas', -1, 'INSERT', NULL, '{\"id\": 5, \"activo\": true, \"nombre\": \"Lima\", \"region\": \"Lima\", \"codigoZona\": \"ZONA-PUNO\", \"totalSucursales\": 0}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-07 03:39:07'),
(11, 'zonas', -1, 'UPDATE', NULL, '{\"id\": 5, \"activo\": false, \"nombre\": \"Lima\", \"region\": \"Lima\", \"codigoZona\": \"ZONA-PUNO\", \"totalSucursales\": 0}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-07 03:42:58'),
(12, 'zonas', -1, 'UPDATE', NULL, '{\"id\": 5, \"activo\": false, \"nombre\": \"Lima\", \"region\": \"Lima\", \"codigoZona\": \"ZONA-PUNO\", \"totalSucursales\": 0}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-07 03:43:42'),
(13, 'zonas', -1, 'UPDATE', NULL, '{\"id\": 5, \"activo\": false, \"nombre\": \"Lima\", \"region\": \"Lima\", \"codigoZona\": \"Z122080\", \"totalSucursales\": 0}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-07 03:45:25'),
(14, 'zonas', -1, 'UPDATE', NULL, '{\"id\": 5, \"activo\": false, \"nombre\": \"Lima\", \"region\": \"Lima\", \"codigoZona\": \"Z122090\", \"totalSucursales\": 0}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-07 03:45:41'),
(15, 'items_kit', -1, 'UPDATE', NULL, '{\"id\": 2, \"estado\": \"DEFECTUOSO\", \"loteId\": 1, \"zonaId\": 4, \"serieSim\": \"001310899586\", \"serieDeco\": \"T33XA1024TT0VE\", \"tieneDeco\": true, \"productoId\": 1, \"zonaNombre\": \"Apurimac\", \"modeloKitId\": 2, \"fechaIngreso\": \"2026-05-04T15:42:06Z\", \"numeroPedido\": \"778202\", \"serieMaestro\": \"K10RBA20A0209\", \"productoNombre\": \"Kit Prepago DirecTV Basico\", \"modeloKitCodigo\": \"LH300\", \"numeroOperacion\": \"4787723\", \"sucursalActualId\": 9, \"custodioActualNombre\": \"Nora Flores Alca\", \"sucursalActualNombre\": \"Oficina Abancay\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-10 01:30:54'),
(16, 'items_kit', -1, 'UPDATE', NULL, '{\"id\": 2, \"estado\": \"DEVUELTO\", \"loteId\": 1, \"zonaId\": 4, \"serieSim\": \"001310899586\", \"serieDeco\": \"T33XA1024TT0VE\", \"tieneDeco\": true, \"productoId\": 1, \"zonaNombre\": \"Apurimac\", \"modeloKitId\": 2, \"fechaIngreso\": \"2026-05-04T15:42:06Z\", \"numeroPedido\": \"778202\", \"serieMaestro\": \"K10RBA20A0209\", \"productoNombre\": \"Kit Prepago DirecTV Basico\", \"modeloKitCodigo\": \"LH300\", \"numeroOperacion\": \"4787723\", \"sucursalActualId\": 9, \"custodioActualNombre\": \"Nora Flores Alca\", \"sucursalActualNombre\": \"Oficina Abancay\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-10 01:31:35'),
(17, 'lotes', -1, 'INSERT', NULL, '{\"id\": 9, \"zonaId\": 1, \"createdAt\": \"2026-05-09T20:58:33.148449Z\", \"zonaNombre\": \"Puno\", \"fechaPedido\": \"2026-05-01\", \"numeroPedido\": \"815673\", \"observaciones\": \"Lote de reposición mensual para oficina central.\", \"fechaRecepcion\": \"2026-05-09\", \"numeroOperacion\": \"7868749\", \"cantidadEsperada\": 2, \"cantidadRecibida\": 2, \"zonaCodigoDirecTV\": \"Z122000\", \"sucursalRecepcionId\": 5, \"usuarioRegistroNombre\": \"Yhomar Torres Mamani\", \"sucursalRecepcionNombre\": \"Oficina 1 Juliaca\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-10 01:58:33'),
(18, 'modelos_kit', -1, 'INSERT', NULL, '{\"id\": 3, \"activo\": true, \"codigo\": \"K-DIR-SD-01\", \"nombre\": \"Kit Prepago SD Estándar\", \"tieneDeco\": true, \"descripcion\": \"Incluye antena, decodificador SD y tarjeta SIM.\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-10 05:56:49'),
(19, 'despachos', -1, 'INSERT', NULL, '{\"id\": 9, \"estado\": \"EN_TRANSITO\", \"createdAt\": \"2026-05-10T02:07:15.129101Z\", \"totalItems\": 2, \"esInterZona\": true, \"guiaRemision\": \"T001-00004562\", \"zonaOrigenId\": 4, \"fechaDespacho\": \"2026-05-10T02:07:15.129101800Z\", \"observaciones\": \"Traslado urgente de stock para la sucursal Juliaca.\", \"zonaDestinoId\": 1, \"fechaRecepcion\": null, \"sucursalOrigenId\": 9, \"zonaOrigenNombre\": \"Apurimac\", \"sucursalDestinoId\": 8, \"zonaDestinoNombre\": \"Puno\", \"usuarioEnviaNombre\": \"Yhomar Torres Mamani\", \"usuarioRecibeNombre\": null, \"sucursalOrigenNombre\": \"Oficina Abancay\", \"sucursalDestinoNombre\": \"Oficina 2 Juliaca Quilca\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-10 07:07:15'),
(20, 'despachos', -1, 'UPDATE', NULL, '{\"mensaje\": \"Recepción registrada. 1 OK, 1 defectuosos, 0 no recibidos.\", \"despachoId\": 9, \"estadoFinal\": \"RECIBIDO_CON_OBSERVACIONES\", \"kitsDefectuosos\": 1, \"kitsNoRecibidos\": 0, \"kitsRecibidosOk\": 1}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-10 07:15:43'),
(21, 'ventas', -1, 'INSERT', NULL, '{\"mensaje\": \"Venta registrada correctamente. Activación pendiente.\", \"ventaId\": 9, \"serieSim\": \"001313100234\", \"fechaVenta\": \"2026-05-12T21:13:55.674693Z\", \"activacionId\": 9, \"serieMaestro\": \"K10RBD20Y2428\", \"clienteNombreCompleto\": \"Carlos Isaac Ramos Mamani\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 02:13:56'),
(22, 'ventas', -1, 'UPDATE', NULL, '{\"id\": 9, \"estado\": \"ANULADA\", \"serieSim\": \"001313100234\", \"clienteId\": 15, \"condicion\": \"CONTADO\", \"itemKitId\": 24, \"clienteDni\": \"70654321\", \"fechaVenta\": \"2026-05-05T21:13:56Z\", \"metodoPago\": \"YAPE\", \"montoVenta\": 110.0, \"vendedorId\": 4, \"zonaNombre\": \"Apurimac\", \"clienteTipo\": \"GENERAL\", \"serieMaestro\": \"K10RBD20Y2428\", \"liquidacionId\": 7, \"montoLiquidado\": 110.0, \"productoNombre\": \"Kit Prepago DirecTV Basico\", \"vendedorNombre\": \"Nora Flores Alca\", \"motivoAnulacion\": \"KitDefectuoso\", \"sucursalVentaId\": 9, \"sucursalVentaNombre\": \"Oficina Abancay\", \"clienteNombreCompleto\": \"Carlos Isaac Ramos Mamani\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 02:25:38'),
(23, 'ventas', 8, 'UPDATE', NULL, '{\"id\": 8, \"estado\": \"ANULADA\", \"serieSim\": \"001311700890\", \"clienteId\": 8, \"condicion\": \"CONTADO\", \"itemKitId\": 10, \"clienteDni\": \"12345678\", \"fechaVenta\": \"2025-01-28T12:00:00Z\", \"metodoPago\": \"EFECTIVO\", \"montoVenta\": 110.0, \"vendedorId\": 6, \"zonaNombre\": \"Apurimac\", \"clienteTipo\": \"GENERAL\", \"serieMaestro\": \"K20RCA30K1084\", \"liquidacionId\": 2, \"montoLiquidado\": 110.0, \"productoNombre\": \"Kit Prepago DirecTV Plus\", \"vendedorNombre\": \"Maricarmen Sanchez Paz\", \"motivoAnulacion\": \"KitDefectuoso\", \"sucursalVentaId\": 9, \"sucursalVentaNombre\": \"Oficina Abancay\", \"clienteNombreCompleto\": \"Rosa Elena Puma Quispe\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 06:21:57'),
(24, 'lotes', 10, 'INSERT', NULL, '{\"id\": 10, \"zonaId\": 1, \"createdAt\": \"2026-05-13T01:24:58.766697Z\", \"zonaNombre\": \"Puno\", \"fechaPedido\": \"2026-05-01\", \"numeroPedido\": \"815674\", \"observaciones\": \"Lote de reposición mensual para oficina central.\", \"fechaRecepcion\": \"2026-05-09\", \"numeroOperacion\": \"7868750\", \"cantidadEsperada\": 2, \"cantidadRecibida\": 2, \"zonaCodigoDirecTV\": \"Z122000\", \"sucursalRecepcionId\": 5, \"usuarioRegistroNombre\": \"Yhomar Torres Mamani\", \"sucursalRecepcionNombre\": \"Oficina 1 Juliaca\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 06:24:59'),
(25, 'lotes', 11, 'INSERT', NULL, '{\"id\": 11, \"zonaId\": 1, \"createdAt\": \"2026-05-13T01:26:06.744687Z\", \"zonaNombre\": \"Puno\", \"fechaPedido\": \"2026-05-01\", \"numeroPedido\": \"815675\", \"observaciones\": \"Lote de reposición mensual para oficina central.\", \"fechaRecepcion\": \"2026-05-09\", \"numeroOperacion\": \"7868751\", \"cantidadEsperada\": 2, \"cantidadRecibida\": 2, \"zonaCodigoDirecTV\": \"Z122000\", \"sucursalRecepcionId\": 5, \"usuarioRegistroNombre\": \"Yhomar Torres Mamani\", \"sucursalRecepcionNombre\": \"Oficina 1 Juliaca\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 06:26:07'),
(26, 'sucursales', 12, 'INSERT', NULL, '{\"id\": 12, \"tipo\": \"ALMACEN\", \"activo\": true, \"nombre\": \"Sucursal Central TyG\", \"ubigeo\": \"150101\", \"zonaId\": 1, \"direccion\": \"Av. Los Angeles 123\", \"zonaNombre\": \"Puno\", \"ubicacionFisicaId\": null, \"ubicacionFisicaNombre\": null}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 06:50:29'),
(27, 'sucursales', 13, 'INSERT', NULL, '{\"id\": 13, \"tipo\": \"ALMACEN\", \"activo\": true, \"nombre\": \"Sucursal Central TyG\", \"ubigeo\": \"150101\", \"zonaId\": 1, \"direccion\": \"Av. Los Angeles 123\", \"zonaNombre\": \"Puno\", \"ubicacionFisicaId\": null, \"ubicacionFisicaNombre\": null}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 06:52:08'),
(28, 'despachos', 13, 'INSERT', NULL, '{\"id\": 13, \"estado\": \"EN_TRANSITO\", \"createdAt\": \"2026-05-13T01:53:39.354771Z\", \"totalItems\": 1, \"esInterZona\": true, \"guiaRemision\": \"T001-00004562\", \"zonaOrigenId\": 1, \"fechaDespacho\": \"2026-05-13T01:53:39.354771Z\", \"observaciones\": \"Traslado urgente de stock para la sucursal Juliaca.\", \"zonaDestinoId\": 4, \"fechaRecepcion\": null, \"sucursalOrigenId\": 8, \"zonaOrigenNombre\": \"Puno\", \"sucursalDestinoId\": 9, \"zonaDestinoNombre\": \"Apurimac\", \"usuarioEnviaNombre\": \"Yhomar Torres Mamani\", \"usuarioRecibeNombre\": null, \"sucursalOrigenNombre\": \"Oficina 2 Juliaca Quilca\", \"sucursalDestinoNombre\": \"Oficina Abancay\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 06:53:39'),
(29, 'lotes', 12, 'INSERT', NULL, '{\"id\": 12, \"zonaId\": 1, \"createdAt\": \"2026-05-13T01:54:42.964190Z\", \"zonaNombre\": \"Puno\", \"fechaPedido\": \"2026-05-01\", \"numeroPedido\": \"815676\", \"observaciones\": \"Lote de reposición mensual para oficina central.\", \"fechaRecepcion\": \"2026-05-09\", \"numeroOperacion\": \"7868752\", \"cantidadEsperada\": 2, \"cantidadRecibida\": 2, \"zonaCodigoDirecTV\": \"Z122000\", \"sucursalRecepcionId\": 5, \"usuarioRegistroNombre\": \"Yhomar Torres Mamani\", \"sucursalRecepcionNombre\": \"Oficina 1 Juliaca\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 06:54:43'),
(30, 'sucursales', 14, 'INSERT', NULL, '{\"id\": 14, \"tipo\": \"ALMACEN\", \"activo\": true, \"nombre\": \"Sucursal Central TyG\", \"ubigeo\": \"150101\", \"zonaId\": 1, \"direccion\": \"Av. Los Angeles 123\", \"zonaNombre\": \"Puno\", \"ubicacionFisicaId\": null, \"ubicacionFisicaNombre\": null}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 06:55:42'),
(31, 'clientes', 18, 'INSERT', NULL, '{\"id\": 18, \"dni\": \"40556681\", \"ruc\": \"20601234571\", \"tipo\": \"PDV\", \"nombres\": \"Juan Alberto\", \"telefono\": \"051321429\", \"apellidos\": \"Pérez Ramos\", \"createdAt\": \"2026-05-13T01:59:54.768820Z\", \"razonSocial\": \"Comunicaciones Juliaca S.A.C.\", \"nombreCompleto\": \"Juan Alberto Pérez Ramos\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-13 06:59:55'),
(32, 'despachos', -1, 'UPDATE', NULL, '{\"mensaje\": \"Recepción registrada. 1 OK, 0 defectuosos, 0 no recibidos.\", \"despachoId\": 5, \"estadoFinal\": \"RECIBIDO\", \"kitsDefectuosos\": 0, \"kitsNoRecibidos\": 0, \"kitsRecibidosOk\": 1}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-30 05:33:54'),
(33, 'despachos', 21, 'INSERT', NULL, '{\"id\": 21, \"estado\": \"EN_TRANSITO\", \"createdAt\": \"2026-05-30T01:46:10.430662Z\", \"totalItems\": 1, \"esInterZona\": false, \"guiaRemision\": \"T001-00004562\", \"zonaOrigenId\": 1, \"fechaDespacho\": \"2026-05-30T01:46:10.430662Z\", \"observaciones\": \"movimiento\", \"zonaDestinoId\": 1, \"fechaRecepcion\": null, \"sucursalOrigenId\": 11, \"zonaOrigenNombre\": \"Puno\", \"sucursalDestinoId\": 1, \"zonaDestinoNombre\": \"Puno\", \"usuarioEnviaNombre\": \"Yhomar Torres Mamani\", \"usuarioRecibeNombre\": null, \"sucursalOrigenNombre\": \"Sucursal Central TyG\", \"sucursalDestinoNombre\": \"Almacen Puno - Juliaca\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-30 06:46:10'),
(34, 'despachos', 28, 'INSERT', NULL, '{\"id\": 28, \"estado\": \"EN_TRANSITO\", \"createdAt\": \"2026-05-30T03:39:17.388944Z\", \"totalItems\": 1, \"esInterZona\": false, \"guiaRemision\": \"T001-00004565\", \"zonaOrigenId\": 1, \"fechaDespacho\": \"2026-05-30T03:39:17.388944700Z\", \"observaciones\": \"MV\", \"zonaDestinoId\": 1, \"fechaRecepcion\": null, \"sucursalOrigenId\": 11, \"zonaOrigenNombre\": \"Puno\", \"sucursalDestinoId\": 1, \"zonaDestinoNombre\": \"Puno\", \"usuarioEnviaNombre\": \"Yhomar Torres Mamani\", \"usuarioRecibeNombre\": null, \"sucursalOrigenNombre\": \"Sucursal Central TyG\", \"sucursalDestinoNombre\": \"Almacen Puno - Juliaca\"}', 1, '0:0:0:0:0:0:0:1', NULL, '2026-05-30 08:39:17'),
(35, 'ventas', -1, 'INSERT', NULL, '{\"mensaje\": \"Venta registrada correctamente. Activación pendiente.\", \"ventaId\": 10, \"serieSim\": \"001311500678\", \"fechaVenta\": \"2026-05-31T01:36:45.244242Z\", \"activacionId\": 10, \"serieMaestro\": \"K10RBA70I0862\", \"clienteNombreCompleto\": \"Juan Alberto Pérez Ramos\"}', 4, '0:0:0:0:0:0:0:1', NULL, '2026-05-31 06:36:45');

-- --------------------------------------------------------

--
-- Table structure for table `clientes`
--

CREATE TABLE `clientes` (
  `id` bigint NOT NULL,
  `dni` varchar(20) NOT NULL,
  `nombres` varchar(255) NOT NULL,
  `apellidos` varchar(255) NOT NULL,
  `telefono` varchar(20) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `tipo` enum('PDV','GENERAL') NOT NULL DEFAULT 'GENERAL' COMMENT 'PDV = Punto de Venta (empresa/tienda). GENERAL = persona natural',
  `razon_social` varchar(255) DEFAULT NULL COMMENT 'Solo para tipo PDV',
  `ruc` varchar(15) DEFAULT NULL COMMENT 'Solo para tipo PDV'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Consumidores finales (PDV). El DNI es el identificador unico';

--
-- Dumping data for table `clientes`
--

INSERT INTO `clientes` (`id`, `dni`, `nombres`, `apellidos`, `telefono`, `created_at`, `tipo`, `razon_social`, `ruc`) VALUES
(1, '74129067', 'Brayan', 'Condori Quispe', '951234567', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(2, '45678901', 'Ana Maria', 'Huanca Flores', '952345678', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(3, '32109876', 'Jose Luis', 'Mamani Turpo', '953456789', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(4, '67890123', 'Carmen', 'Quispe Layme', '954567890', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(5, '23456789', 'Eduardo', 'Apaza Calcina', '955678901', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(6, '89012345', 'Lucia', 'Calisaya Ramos', '956789012', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(7, '56789012', 'Miguel', 'Chura Mamani', '957890123', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(8, '12345678', 'Rosa Elena', 'Puma Quispe', '958901234', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(9, '78901234', 'Carlos', 'Vilca Sucari', '959012345', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(10, '34567890', 'Yolanda', 'Ccama Huanca', '960123456', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(11, '90123456', 'Freddy', 'Llanqui Corimanya', '961234567', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(12, '46789012', 'Sandra', 'Mullisaca Torres', '962345678', '2026-05-04 20:42:06', 'GENERAL', NULL, NULL),
(13, '71234567', 'Nate', 'Sistemas', '987654321', '2026-05-13 01:00:27', 'GENERAL', NULL, NULL),
(14, '40556677', 'Juan Alberto', 'Pérez Ramos', '051321456', '2026-05-13 01:00:52', 'PDV', 'Comunicaciones Juliaca S.A.C.', '20601234567'),
(15, '70654321', 'Carlos Isaac', 'Ramos Mamani', '951443322', '2026-05-13 02:13:56', 'GENERAL', NULL, NULL),
(20, '40556681', 'Juan Alberto', 'Pérez Ramos', '051321429', '2026-05-13 07:06:46', 'PDV', 'Comunicaciones Juliaca S.A.C.', '20601234571');

-- --------------------------------------------------------

--
-- Table structure for table `despachos`
--

CREATE TABLE `despachos` (
  `id` bigint NOT NULL,
  `sucursal_origen_id` bigint NOT NULL,
  `sucursal_destino_id` bigint NOT NULL,
  `estado` enum('PREPARANDO','EN_TRANSITO','RECIBIDO','RECIBIDO_CON_OBSERVACIONES','CANCELADO') NOT NULL DEFAULT 'PREPARANDO',
  `usuario_envia_id` bigint NOT NULL,
  `usuario_recibe_id` bigint DEFAULT NULL COMMENT 'Se asigna al momento de confirmar recepcion',
  `guia_remision` varchar(100) DEFAULT NULL COMMENT 'Numero de guia de remision fisica (documento SUNAT)',
  `observaciones` text,
  `fecha_despacho` timestamp NULL DEFAULT NULL,
  `fecha_recepcion` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Cabecera de cada traslado entre sucursales. Un despacho contiene N kits';

--
-- Dumping data for table `despachos`
--

INSERT INTO `despachos` (`id`, `sucursal_origen_id`, `sucursal_destino_id`, `estado`, `usuario_envia_id`, `usuario_recibe_id`, `guia_remision`, `observaciones`, `fecha_despacho`, `fecha_recepcion`, `created_at`, `updated_at`) VALUES
(1, 3, 9, 'RECIBIDO', 3, 4, 'GR-2025-001', NULL, '2025-01-18 14:00:00', '2025-01-18 19:30:00', '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(2, 2, 7, 'RECIBIDO', 3, 8, 'GR-2025-002', NULL, '2025-01-23 13:00:00', '2025-01-23 16:00:00', '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(3, 1, 5, 'RECIBIDO', 3, 7, 'GR-2025-003', NULL, '2025-02-06 14:30:00', '2025-02-06 15:00:00', '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(4, 1, 6, 'RECIBIDO', 3, 5, 'GR-2025-004', NULL, '2025-02-06 14:30:00', '2025-02-06 15:15:00', '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(5, 3, 9, 'RECIBIDO', 3, 1, 'GR-2025-005', NULL, '2025-03-01 14:00:00', '2026-05-30 05:33:54', '2026-05-04 20:42:06', '2026-05-30 05:33:54'),
(6, 4, 10, 'RECIBIDO_CON_OBSERVACIONES', 3, NULL, 'GR-2025-006', NULL, '2025-02-13 13:00:00', '2025-02-13 21:00:00', '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(9, 9, 8, 'RECIBIDO_CON_OBSERVACIONES', 1, 1, 'T001-00004562', 'Llegó el camión de logística con el precinto de seguridad íntegro. Se procede a la revisión individual.', '2026-05-10 07:07:15', '2026-05-10 07:15:43', '2026-05-10 07:07:15', '2026-05-10 07:15:43'),
(13, 8, 9, 'EN_TRANSITO', 1, NULL, 'T001-00004562', 'Traslado urgente de stock para la sucursal Juliaca.', '2026-05-13 06:53:39', NULL, '2026-05-13 06:53:39', '2026-05-13 06:53:39'),
(21, 11, 1, 'EN_TRANSITO', 1, NULL, 'T001-00004562', 'movimiento', '2026-05-30 06:46:10', NULL, '2026-05-30 06:46:10', '2026-05-30 06:46:10'),
(28, 11, 1, 'EN_TRANSITO', 1, NULL, 'T001-00004565', 'MV', '2026-05-30 08:39:17', NULL, '2026-05-30 08:39:17', '2026-05-30 08:39:17');

-- --------------------------------------------------------

--
-- Table structure for table `despacho_items`
--

CREATE TABLE `despacho_items` (
  `id` bigint NOT NULL,
  `despacho_id` bigint NOT NULL,
  `item_kit_id` bigint NOT NULL,
  `estado_item` enum('ENVIADO','RECIBIDO_OK','RECIBIDO_DEFECTUOSO','NO_RECIBIDO') NOT NULL DEFAULT 'ENVIADO',
  `observacion` varchar(500) DEFAULT NULL COMMENT 'Descripcion del defecto si estado=RECIBIDO_DEFECTUOSO'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Detalle item a item de cada despacho. Permite observaciones individuales al recepcionar';

--
-- Dumping data for table `despacho_items`
--

INSERT INTO `despacho_items` (`id`, `despacho_id`, `item_kit_id`, `estado_item`, `observacion`) VALUES
(1, 1, 1, 'RECIBIDO_OK', NULL),
(2, 1, 2, 'RECIBIDO_OK', NULL),
(3, 1, 3, 'RECIBIDO_OK', NULL),
(4, 1, 4, 'RECIBIDO_OK', NULL),
(5, 1, 5, 'RECIBIDO_OK', NULL),
(6, 1, 6, 'RECIBIDO_OK', NULL),
(7, 1, 10, 'RECIBIDO_OK', NULL),
(8, 2, 11, 'RECIBIDO_OK', NULL),
(9, 2, 12, 'RECIBIDO_OK', NULL),
(10, 2, 13, 'RECIBIDO_OK', NULL),
(11, 2, 15, 'RECIBIDO_OK', NULL),
(12, 2, 16, 'RECIBIDO_OK', NULL),
(13, 3, 17, 'RECIBIDO_OK', NULL),
(14, 4, 18, 'RECIBIDO_OK', NULL),
(15, 4, 19, 'RECIBIDO_OK', NULL),
(16, 4, 22, 'RECIBIDO_OK', NULL),
(17, 5, 8, 'RECIBIDO_OK', NULL),
(18, 6, 23, 'RECIBIDO_OK', NULL),
(19, 6, 24, 'RECIBIDO_OK', NULL),
(20, 6, 25, 'RECIBIDO_DEFECTUOSO', 'Caja abierta, SIM con rayones en chip. Se acepta condicionalmente pendiente de prueba de activacion'),
(21, 9, 3, 'RECIBIDO_OK', 'Caja sellada'),
(22, 9, 4, 'RECIBIDO_DEFECTUOSO', 'Antena con abolladura lateral, se reporta para baja técnica.'),
(23, 13, 3, 'ENVIADO', NULL),
(24, 21, 7, 'ENVIADO', NULL),
(25, 28, 20, 'ENVIADO', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `historial_custodios`
--

CREATE TABLE `historial_custodios` (
  `id` bigint NOT NULL,
  `item_kit_id` bigint NOT NULL,
  `sucursal_anterior_id` bigint DEFAULT NULL,
  `sucursal_nueva_id` bigint DEFAULT NULL,
  `custodio_anterior_id` bigint DEFAULT NULL,
  `custodio_nuevo_id` bigint DEFAULT NULL,
  `tipo_evento` enum('INGRESO','TRASLADO','ASIGNACION','DEVOLUCION','VENTA','BAJA') NOT NULL,
  `motivo` varchar(500) DEFAULT NULL,
  `referencia_id` bigint DEFAULT NULL COMMENT 'ID del despacho, venta u operacion que origino el cambio',
  `referencia_tipo` varchar(50) DEFAULT NULL COMMENT 'Valores: DESPACHO | VENTA | AJUSTE_MANUAL',
  `registrado_por_id` bigint NOT NULL,
  `fecha_evento` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Linea de vida de cada kit. NUNCA UPDATE ni DELETE. Permite reconstruir la ubicacion de cualquier serial en cualquier fecha pasada';

--
-- Dumping data for table `historial_custodios`
--

INSERT INTO `historial_custodios` (`id`, `item_kit_id`, `sucursal_anterior_id`, `sucursal_nueva_id`, `custodio_anterior_id`, `custodio_nuevo_id`, `tipo_evento`, `motivo`, `referencia_id`, `referencia_tipo`, `registrado_por_id`, `fecha_evento`) VALUES
(1, 1, NULL, 3, NULL, 3, 'INGRESO', 'Recepcion lote 778202 en Almacen Apurimac', 1, 'DESPACHO', 3, '2025-01-17 13:00:00'),
(2, 1, 3, 9, 3, 6, 'TRASLADO', 'Despacho GR-2025-001 Apurimac->Abancay', 1, 'DESPACHO', 3, '2025-01-18 19:30:00'),
(3, 1, 9, 9, 6, NULL, 'VENTA', 'Vendido a Brayan Condori DNI 74129067', 1, 'VENTA', 6, '2025-01-20 15:30:00'),
(4, 5, NULL, 3, NULL, 3, 'INGRESO', 'Recepcion lote 778202 en Almacen Apurimac', 1, 'DESPACHO', 3, '2025-01-17 13:00:00'),
(5, 5, 3, 9, 3, 4, 'TRASLADO', 'Despacho GR-2025-001 Apurimac->Abancay', 1, 'DESPACHO', 3, '2025-01-18 19:30:00'),
(6, 5, 9, 9, 4, NULL, 'VENTA', 'Vendido a Ana Maria Huanca', 2, 'VENTA', 4, '2025-01-22 20:00:00'),
(7, 9, NULL, 3, NULL, 3, 'INGRESO', 'Recepcion lote 778202. Detectado defecto fisico en antena', 1, 'DESPACHO', 3, '2025-01-17 13:00:00'),
(8, 9, 3, 3, 3, 3, 'BAJA', 'Defecto de fabrica: conector antena roto. Reportado a DirecTV', NULL, 'AJUSTE_MANUAL', 3, '2025-01-17 14:00:00'),
(9, 11, NULL, 2, NULL, 3, 'INGRESO', 'Recepcion lote 790451 en Almacen Cusco', 2, 'DESPACHO', 3, '2025-01-22 13:00:00'),
(10, 11, 2, 7, 3, 8, 'TRASLADO', 'Despacho GR-2025-002 Cusco->Oficina Centro', 2, 'DESPACHO', 3, '2025-01-23 16:00:00'),
(11, 11, 7, 7, 8, NULL, 'VENTA', 'Vendido a Carmen Quispe', 4, 'VENTA', 8, '2025-01-24 14:00:00'),
(12, 19, NULL, 1, NULL, 2, 'INGRESO', 'Recepcion lote 801233 en Almacen Juliaca', 3, 'DESPACHO', 3, '2025-02-05 13:00:00'),
(13, 19, 1, 6, 2, 5, 'TRASLADO', 'Despacho GR-2025-004 Juliaca->Oficina 2', 4, 'DESPACHO', 3, '2025-02-06 15:15:00'),
(14, 19, 6, 6, 5, NULL, 'VENTA', 'Vendido a Miguel Chura Mamani', 7, 'VENTA', 5, '2025-02-10 15:00:00'),
(15, 25, NULL, 4, NULL, NULL, 'INGRESO', 'Recepcion lote 815670 en Almacen Madre de Dios', 4, 'DESPACHO', 3, '2025-02-12 13:00:00'),
(16, 25, 4, 10, NULL, NULL, 'TRASLADO', 'Despacho GR-2025-006. Kit con observacion menor', 6, 'DESPACHO', 3, '2025-02-13 21:00:00'),
(17, 25, 10, 10, NULL, NULL, 'VENTA', 'Vendido por freelance Ricardo Mamani a Lucia Calisaya', 6, 'VENTA', 1, '2025-02-14 21:00:00'),
(18, 2, 9, 9, 4, 1, 'BAJA', 'Pantalla trizada detectada durante la inspección de ingreso.', NULL, 'AJUSTE_MANUAL', 1, '2026-05-10 01:30:54'),
(19, 2, 9, 9, 4, 1, 'BAJA', 'Pedido cancelado por el cliente antes de la entrega final.', NULL, 'AJUSTE_MANUAL', 1, '2026-05-10 01:31:35'),
(22, 28, NULL, 5, NULL, 1, 'INGRESO', 'Recepción lote N° 815673', 9, 'LOTE', 1, '2026-05-10 01:58:33'),
(23, 29, NULL, 5, NULL, 1, 'INGRESO', 'Recepción lote N° 815673', 9, 'LOTE', 1, '2026-05-10 01:58:33'),
(24, 3, 9, 8, 4, NULL, 'TRASLADO', 'Despacho T001-00004562 → Oficina 2 Juliaca Quilca', 9, 'DESPACHO', 1, '2026-05-10 07:07:15'),
(25, 4, 9, 8, 6, NULL, 'TRASLADO', 'Despacho T001-00004562 → Oficina 2 Juliaca Quilca', 9, 'DESPACHO', 1, '2026-05-10 07:07:15'),
(26, 3, 9, 8, NULL, 1, 'TRASLADO', 'Recibido OK en Oficina 2 Juliaca Quilca', 9, 'DESPACHO', 1, '2026-05-10 07:15:43'),
(27, 4, 9, 8, NULL, 1, 'BAJA', 'Defectuoso al recibir: Antena con abolladura lateral, se reporta para baja técnica.', 9, 'DESPACHO', 1, '2026-05-10 07:15:43'),
(28, 24, 10, 9, NULL, 4, 'VENTA', 'Vendido a Carlos Isaac Ramos Mamani DNI: 70654321', 9, 'VENTA', 1, '2026-05-13 02:13:56'),
(29, 24, 9, 9, NULL, NULL, 'DEVOLUCION', 'Venta anulada. Motivo: KitDefectuoso', 9, 'VENTA', 1, '2026-05-13 02:25:38'),
(30, 10, 9, 9, NULL, NULL, 'DEVOLUCION', 'Venta anulada. Motivo: KitDefectuoso', 8, 'VENTA', 1, '2026-05-13 06:21:57'),
(31, 30, NULL, 5, NULL, 1, 'INGRESO', 'Recepción lote N° 815674', 10, 'LOTE', 1, '2026-05-13 06:24:59'),
(32, 31, NULL, 5, NULL, 1, 'INGRESO', 'Recepción lote N° 815674', 10, 'LOTE', 1, '2026-05-13 06:24:59'),
(33, 32, NULL, 5, NULL, 1, 'INGRESO', 'Recepción lote N° 815675', 11, 'LOTE', 1, '2026-05-13 06:26:07'),
(34, 33, NULL, 5, NULL, 1, 'INGRESO', 'Recepción lote N° 815675', 11, 'LOTE', 1, '2026-05-13 06:26:07'),
(36, 3, 8, 9, 1, NULL, 'TRASLADO', 'Despacho T001-00004562 → Oficina Abancay', 13, 'DESPACHO', 1, '2026-05-13 06:53:39'),
(37, 34, NULL, 5, NULL, 1, 'INGRESO', 'Recepción lote N° 815676', 12, 'LOTE', 1, '2026-05-13 06:54:43'),
(38, 35, NULL, 5, NULL, 1, 'INGRESO', 'Recepción lote N° 815676', 12, 'LOTE', 1, '2026-05-13 06:54:43'),
(39, 8, 3, 9, NULL, 1, 'TRASLADO', 'Recibido OK en Oficina Abancay', 5, 'DESPACHO', 1, '2026-05-30 05:33:54'),
(40, 7, 11, 1, 3, NULL, 'TRASLADO', 'Despacho T001-00004562 → Almacen Puno - Juliaca', 21, 'DESPACHO', 1, '2026-05-30 06:46:10'),
(41, 20, 11, 1, 2, NULL, 'TRASLADO', 'Despacho T001-00004565 → Almacen Puno - Juliaca', 28, 'DESPACHO', 1, '2026-05-30 08:39:17'),
(42, 8, 11, 9, 1, 4, 'VENTA', 'Vendido a Juan Alberto Pérez Ramos DNI: 40556681', 10, 'VENTA', 4, '2026-05-31 06:36:45');

-- --------------------------------------------------------

--
-- Table structure for table `items_kit`
--

CREATE TABLE `items_kit` (
  `id` bigint NOT NULL,
  `lote_id` bigint NOT NULL,
  `producto_id` bigint NOT NULL,
  `modelo_kit_id` bigint DEFAULT NULL,
  `serie_maestro` varchar(100) NOT NULL,
  `serie_sim` varchar(50) NOT NULL COMMENT 'Campo clave para activacion DirecTV. Solo serie y SIM segun nota del Excel',
  `serie_deco` varchar(100) DEFAULT NULL COMMENT 'NULL permitido para kits sin decodificador. Si tiene valor, debe ser único a nivel global',
  `estado` enum('DISPONIBLE','VENDIDO','TRANSITO','DEFECTUOSO','DEVUELTO') NOT NULL DEFAULT 'DISPONIBLE',
  `sucursal_actual_id` bigint DEFAULT NULL,
  `custodio_actual_id` bigint DEFAULT NULL,
  `fecha_ingreso` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Unidad minima de inventario: un kit fisico serializado. Stock real = COUNT(*) WHERE estado=DISPONIBLE AND sucursal_actual_id=X';

--
-- Dumping data for table `items_kit`
--

INSERT INTO `items_kit` (`id`, `lote_id`, `producto_id`, `modelo_kit_id`, `serie_maestro`, `serie_sim`, `serie_deco`, `estado`, `sucursal_actual_id`, `custodio_actual_id`, `fecha_ingreso`, `updated_at`) VALUES
(1, 1, 1, 1, 'K10RBA50E0093', '001311006579', 'T33XA1024TS14W', 'VENDIDO', 9, 6, '2026-05-04 20:42:06', '2026-05-09 19:40:27'),
(2, 1, 1, 2, 'K10RBA20A0209', '001310899586', 'T33XA1024TT0VE', 'DEVUELTO', 9, 4, '2026-05-04 20:42:06', '2026-05-10 01:31:35'),
(3, 1, 1, 1, 'K10RBA30C0317', '001310988401', 'T33XB2031UA1FA', 'TRANSITO', 8, 1, '2026-05-04 20:42:06', '2026-05-13 06:53:39'),
(4, 1, 1, 2, 'K10RBA40D0428', '001311100234', 'T33XB2031UB2GB', 'DEFECTUOSO', 8, 6, '2026-05-04 20:42:06', '2026-05-10 07:15:43'),
(5, 1, 2, 1, 'K20RCA10F0539', '001311200345', 'T44XC3042VC3HC', 'VENDIDO', 9, 6, '2026-05-04 20:42:06', '2026-05-09 19:40:42'),
(6, 1, 2, 2, 'K20RCA20G0640', '001311300456', 'T44XC3042VD4ID', 'VENDIDO', 9, 4, '2026-05-04 20:42:06', '2026-05-09 19:40:45'),
(7, 1, 1, 1, 'K10RBA60H0751', '001311400567', 'T33XD4053WE5JE', 'TRANSITO', 11, 3, '2026-05-04 20:42:06', '2026-05-30 06:46:10'),
(8, 1, 1, 1, 'K10RBA70I0862', '001311500678', 'T33XD4053WF6KF', 'VENDIDO', 11, 1, '2026-05-04 20:42:06', '2026-05-31 06:36:45'),
(9, 1, 1, 1, 'K10RBA80J0973', '001311600789', 'T33XE5064XG7LG', 'DEFECTUOSO', 3, 3, '2026-05-04 20:42:06', '2026-05-09 19:40:56'),
(10, 1, 2, 2, 'K20RCA30K1084', '001311700890', 'T44XE5064XH8MH', 'DISPONIBLE', 9, 1, '2026-05-04 20:42:06', '2026-05-13 06:21:57'),
(11, 2, 1, 1, 'K10RBB10L1195', '001311800901', 'T33XF6075YI9NI', 'VENDIDO', 7, 8, '2026-05-04 20:42:06', '2026-05-09 19:41:04'),
(12, 2, 1, 2, 'K10RBB20M1206', '001311900012', 'T33XF6075YJ0OJ', 'DISPONIBLE', 7, 8, '2026-05-04 20:42:06', '2026-05-09 19:41:07'),
(13, 2, 2, 2, 'K20RCB10N1317', '001312000123', 'T44XG7086ZK1PK', 'VENDIDO', 7, 8, '2026-05-04 20:42:06', '2026-05-09 19:41:09'),
(14, 2, 1, 2, 'K10RBB30O1428', '001312100234', 'T33XG7086ZL2QL', 'DISPONIBLE', 2, 3, '2026-05-04 20:42:06', '2026-05-09 19:41:12'),
(15, 2, 1, 1, 'K10RBB40P1539', '001312200345', 'T33XH8097AM3RM', 'DISPONIBLE', 7, 8, '2026-05-04 20:42:06', '2026-05-09 19:41:16'),
(16, 2, 2, 1, 'K20RCB20Q1640', '001312300456', 'T44XH8097AN4SN', 'DISPONIBLE', 7, 8, '2026-05-04 20:42:06', '2026-05-09 19:41:19'),
(17, 3, 1, 1, 'K10RBC10R1751', '001312400567', 'T33XI9108BO5TO', 'DISPONIBLE', 5, 7, '2026-05-04 20:42:06', '2026-05-09 19:41:21'),
(18, 3, 1, 1, 'K10RBC20S1862', '001312500678', 'T33XI9108BP6UP', 'DISPONIBLE', 6, 5, '2026-05-04 20:42:06', '2026-05-09 19:41:23'),
(19, 3, 2, 1, 'K20RCC10T1973', '001312600789', 'T44XJ0119CQ7VQ', 'VENDIDO', 6, 5, '2026-05-04 20:42:06', '2026-05-09 19:41:26'),
(20, 3, 1, 2, 'K10RBC30U2084', '001312700890', 'T33XJ0119CR8WR', 'TRANSITO', 11, 2, '2026-05-04 20:42:06', '2026-05-30 08:39:17'),
(21, 3, 1, 2, 'K10RBC40V2195', '001312800901', 'T33XK1120DS9XS', 'DISPONIBLE', 1, 2, '2026-05-04 20:42:06', '2026-05-09 19:41:32'),
(22, 3, 2, 2, 'K20RCC20W2206', '001312900012', 'T44XK1120DT0YT', 'DISPONIBLE', 6, 5, '2026-05-04 20:42:06', '2026-05-09 19:41:34'),
(23, 4, 1, 2, 'K10RBD10X2317', '001313000123', 'T33XL2131EU1ZU', 'DISPONIBLE', 10, NULL, '2026-05-04 20:42:06', '2026-05-09 19:41:37'),
(24, 4, 1, 1, 'K10RBD20Y2428', '001313100234', 'T33XL2131EV2AV', 'DISPONIBLE', 9, 1, '2026-05-04 20:42:06', '2026-05-13 02:25:38'),
(25, 4, 2, 1, 'K20RCD10Z2539', '001313200345', 'T44XM3142FW3BW', 'VENDIDO', 10, NULL, '2026-05-04 20:42:06', '2026-05-09 19:41:42'),
(28, 9, 2, 1, 'MAESTRO0001', 'SIM0001', 'DECO0001', 'DISPONIBLE', 5, 1, '2026-05-10 01:58:33', '2026-05-10 01:58:33'),
(29, 9, 2, 2, 'MAESTRO0002', 'SIM0002', NULL, 'DISPONIBLE', 5, 1, '2026-05-10 01:58:33', '2026-05-10 01:58:33'),
(30, 10, 2, 1, 'MAESTRO0003', 'SIM0003', 'DECO0003', 'DISPONIBLE', 5, 1, '2026-05-13 06:24:59', '2026-05-13 06:24:59'),
(31, 10, 2, 2, 'MAESTRO0004', 'SIM0004', NULL, 'DISPONIBLE', 5, 1, '2026-05-13 06:24:59', '2026-05-13 06:24:59'),
(32, 11, 2, 1, 'MAESTRO0005', 'SIM0005', 'DECO0005', 'DISPONIBLE', 5, 1, '2026-05-13 06:26:07', '2026-05-13 06:26:07'),
(33, 11, 2, 2, 'MAESTRO0006', 'SIM0006', NULL, 'DISPONIBLE', 5, 1, '2026-05-13 06:26:07', '2026-05-13 06:26:07'),
(34, 12, 2, 1, 'MAESTRO0007', 'SIM0007', 'DECO0007', 'DISPONIBLE', 5, 1, '2026-05-13 06:54:43', '2026-05-13 06:54:43'),
(35, 12, 2, 2, 'MAESTRO0008', 'SIM0008', NULL, 'DISPONIBLE', 5, 1, '2026-05-13 06:54:43', '2026-05-13 06:54:43');

-- --------------------------------------------------------

--
-- Table structure for table `kardex_mensual`
--

CREATE TABLE `kardex_mensual` (
  `id` bigint NOT NULL,
  `sucursal_id` bigint NOT NULL,
  `producto_id` bigint NOT NULL,
  `periodo` varchar(7) NOT NULL COMMENT 'Formato YYYY-MM. Ej: 2025-01',
  `stock_inicio` int NOT NULL DEFAULT '0',
  `total_ingresos` int NOT NULL DEFAULT '0',
  `total_salidas` int NOT NULL DEFAULT '0',
  `stock_fin` int GENERATED ALWAYS AS (((`stock_inicio` + `total_ingresos`) - `total_salidas`)) STORED COMMENT 'MySQL calcula automaticamente. No editable directamente',
  `total_liquidado` decimal(12,2) NOT NULL DEFAULT '0.00' COMMENT 'Suma de montos liquidados a gerencia en el periodo',
  `cerrado` tinyint(1) DEFAULT '0' COMMENT 'TRUE = periodo contable bloqueado para edicion. Solo ADMIN puede reabrir',
  `generado_por_id` bigint DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Kardex mensual por sucursal y producto. stock_fin es columna calculada. cerrado=TRUE bloquea edicion para auditoria del contador';

--
-- Dumping data for table `kardex_mensual`
--

INSERT INTO `kardex_mensual` (`id`, `sucursal_id`, `producto_id`, `periodo`, `stock_inicio`, `total_ingresos`, `total_salidas`, `total_liquidado`, `cerrado`, `generado_por_id`, `created_at`, `updated_at`) VALUES
(1, 9, 1, '2025-01', 0, 6, 3, '330.00', 1, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(2, 9, 2, '2025-01', 0, 2, 1, '140.00', 1, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(3, 7, 1, '2025-01', 0, 3, 1, '110.00', 1, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(4, 7, 2, '2025-01', 0, 2, 1, '140.00', 1, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(5, 6, 1, '2025-02', 0, 2, 0, '0.00', 0, 2, '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(6, 6, 2, '2025-02', 0, 1, 1, '140.00', 0, 2, '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(7, 10, 1, '2025-02', 0, 2, 0, '0.00', 0, 2, '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(8, 10, 2, '2025-02', 0, 1, 1, '80.00', 0, 2, '2026-05-04 20:42:06', '2026-05-04 20:42:06'),
(9, 9, 1, '2026-05', 3, 2, 6, '0.00', 0, 1, '2026-05-13 07:27:53', '2026-05-13 07:27:53'),
(10, 9, 2, '2026-05', 1, 1, 0, '0.00', 0, 1, '2026-05-13 07:27:53', '2026-05-13 07:30:00');

-- --------------------------------------------------------

--
-- Table structure for table `liquidaciones_caja`
--

CREATE TABLE `liquidaciones_caja` (
  `id` bigint NOT NULL,
  `vendedor_id` bigint NOT NULL,
  `sucursal_id` bigint NOT NULL,
  `periodo_inicio` date NOT NULL,
  `periodo_fin` date NOT NULL,
  `monto_total_esperado` decimal(10,2) NOT NULL DEFAULT '0.00',
  `monto_depositado` decimal(10,2) DEFAULT NULL,
  `diferencia` decimal(10,2) GENERATED ALWAYS AS ((`monto_depositado` - `monto_total_esperado`)) STORED COMMENT 'Calculada automaticamente por MySQL. Positivo=sobrante, Negativo=faltante',
  `estado` enum('PENDIENTE','APROBADO','RECHAZADO','OBSERVADO') NOT NULL DEFAULT 'PENDIENTE',
  `aprobado_por_id` bigint DEFAULT NULL,
  `observaciones` text,
  `fecha_liquidacion` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `fecha_aprobacion` timestamp NULL DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Liquidacion de caja por vendedor y periodo. La columna diferencia es calculada automaticamente';

--
-- Dumping data for table `liquidaciones_caja`
--

INSERT INTO `liquidaciones_caja` (`id`, `vendedor_id`, `sucursal_id`, `periodo_inicio`, `periodo_fin`, `monto_total_esperado`, `monto_depositado`, `estado`, `aprobado_por_id`, `observaciones`, `fecha_liquidacion`, `fecha_aprobacion`, `created_at`) VALUES
(1, 4, 9, '2025-01-17', '2025-01-31', '330.00', '330.00', 'APROBADO', 1, NULL, '2025-02-01 14:00:00', '2025-02-02 15:00:00', '2026-05-04 20:42:06'),
(2, 6, 9, '2025-01-17', '2025-01-31', '250.00', '240.00', 'OBSERVADO', 1, NULL, '2025-02-01 14:30:00', NULL, '2026-05-04 20:42:06'),
(3, 8, 7, '2025-01-23', '2025-01-31', '360.00', '360.00', 'APROBADO', 1, NULL, '2025-02-01 15:00:00', '2025-02-02 16:00:00', '2026-05-04 20:42:06'),
(4, 5, 6, '2025-02-06', '2025-02-28', '250.00', '250.00', 'PENDIENTE', NULL, NULL, '2025-03-01 13:00:00', NULL, '2026-05-04 20:42:06'),
(7, 4, 9, '2026-05-01', '2026-05-07', '80.00', '450.00', 'APROBADO', 1, 'Depósito verificado en cuenta BCP. Voucher #99821.', '2026-05-13 01:40:30', '2026-05-13 02:21:03', '2026-05-13 01:40:30');

-- --------------------------------------------------------

--
-- Table structure for table `lotes`
--

CREATE TABLE `lotes` (
  `id` bigint NOT NULL,
  `numero_pedido` varchar(50) DEFAULT NULL COMMENT 'N° de seguimiento del cargamento DirecTV (para Logistica)',
  `numero_operacion` varchar(50) DEFAULT NULL COMMENT 'N° de boucher de compra TyG->DirecTV (para Administracion/Contador)',
  `zona_id` bigint NOT NULL,
  `sucursal_recepcion_id` bigint NOT NULL,
  `cantidad_esperada` int NOT NULL,
  `cantidad_recibida` int DEFAULT '0',
  `fecha_pedido` date DEFAULT NULL,
  `fecha_recepcion` date DEFAULT NULL,
  `observaciones` text,
  `usuario_registro_id` bigint NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Agrupa los kits que llegan en un mismo cargamento. Separa N°Pedido (logistico) de N°Operacion (contable)';

--
-- Dumping data for table `lotes`
--

INSERT INTO `lotes` (`id`, `numero_pedido`, `numero_operacion`, `zona_id`, `sucursal_recepcion_id`, `cantidad_esperada`, `cantidad_recibida`, `fecha_pedido`, `fecha_recepcion`, `observaciones`, `usuario_registro_id`, `created_at`) VALUES
(1, '778202', '4787723', 4, 3, 20, 20, '2025-01-15', '2025-01-17', NULL, 3, '2026-05-04 20:42:06'),
(2, '790451', '5868745', 2, 2, 15, 15, '2025-01-20', '2025-01-22', NULL, 3, '2026-05-04 20:42:06'),
(3, '801233', '7224587', 1, 1, 25, 24, '2025-02-03', '2025-02-05', NULL, 3, '2026-05-04 20:42:06'),
(4, '815670', '7868746', 3, 4, 10, 10, '2025-02-10', '2025-02-12', NULL, 3, '2026-05-04 20:42:06'),
(9, '815673', '7868749', 1, 5, 2, 2, '2026-05-01', '2026-05-09', 'Lote de reposición mensual para oficina central.', 1, '2026-05-10 01:58:33'),
(10, '815674', '7868750', 1, 5, 2, 2, '2026-05-01', '2026-05-09', 'Lote de reposición mensual para oficina central.', 1, '2026-05-13 06:24:59'),
(11, '815675', '7868751', 1, 5, 2, 2, '2026-05-01', '2026-05-09', 'Lote de reposición mensual para oficina central.', 1, '2026-05-13 06:26:07'),
(12, '815676', '7868752', 1, 5, 2, 2, '2026-05-01', '2026-05-09', 'Lote de reposición mensual para oficina central.', 1, '2026-05-13 06:54:43');

-- --------------------------------------------------------

--
-- Table structure for table `modelos_kit`
--

CREATE TABLE `modelos_kit` (
  `id` bigint NOT NULL,
  `codigo` varchar(20) NOT NULL COMMENT 'Ej: LH100, LH300',
  `nombre` varchar(100) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `tiene_deco` tinyint(1) DEFAULT '1' COMMENT 'FALSE = solo SIM, serie_deco será NULL',
  `activo` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `modelos_kit`
--

INSERT INTO `modelos_kit` (`id`, `codigo`, `nombre`, `descripcion`, `tiene_deco`, `activo`, `created_at`) VALUES
(1, 'LH100', 'Kit Prepago LH100', NULL, 1, 1, '2026-05-05 19:17:27'),
(2, 'LH300', 'Kit Prepago LH300 Plus', NULL, 1, 1, '2026-05-05 19:17:27');

-- --------------------------------------------------------

--
-- Table structure for table `productos`
--

CREATE TABLE `productos` (
  `id` bigint NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `precio_regular` decimal(10,2) DEFAULT NULL COMMENT 'PVP para empleados',
  `activo` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Catalogo de productos. Actualmente: Kit Prepago DirecTV';

--
-- Dumping data for table `productos`
--

INSERT INTO `productos` (`id`, `nombre`, `descripcion`, `precio_regular`, `activo`, `created_at`) VALUES
(1, 'Kit Prepago DirecTV Basico', 'Kit incluye: Antena, Decodificador HD, SIM. Plan Basico 32 canales', '110.00', 1, '2026-05-04 20:42:06'),
(2, 'Kit Prepago DirecTV Plus', 'Kit incluye: Antena, Decodificador HD+, SIM. Plan Plus 55 canales', '140.00', 1, '2026-05-04 20:42:06'),
(3, 'Recarga DirecTV 30 dias', 'Recarga de servicio por 30 dias adicionales', '35.00', 1, '2026-05-04 20:42:06');

-- --------------------------------------------------------

--
-- Table structure for table `sucursales`
--

CREATE TABLE `sucursales` (
  `id` bigint NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `tipo` enum('ALMACEN','OFICINA') NOT NULL,
  `zona_id` bigint NOT NULL,
  `ubigeo` varchar(10) NOT NULL,
  `direccion` varchar(255) NOT NULL,
  `ubicacion_fisica_id` bigint DEFAULT NULL COMMENT 'FK self-ref: dos sucursales que comparten direccion fisica (ej: Almacen Juliaca = Oficina 2 Juliaca)',
  `activo` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT 'Soft delete. NULL = activa'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Almacenes y oficinas. Soft delete: nunca DELETE fisico para conservar historial de ventas';

--
-- Dumping data for table `sucursales`
--

INSERT INTO `sucursales` (`id`, `nombre`, `tipo`, `zona_id`, `ubigeo`, `direccion`, `ubicacion_fisica_id`, `activo`, `created_at`, `updated_at`, `deleted_at`) VALUES
(1, 'Almacen Puno - Juliaca', 'ALMACEN', 1, '210101', 'Av. Ferroviaria 450, Juliaca', NULL, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06', NULL),
(2, 'Almacen Cusco', 'ALMACEN', 2, '080101', 'Av. La Cultura 1234, Cusco', NULL, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06', NULL),
(3, 'Almacen Apurimac', 'ALMACEN', 4, '030101', 'Jr. Arequipa 890, Abancay', NULL, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06', NULL),
(4, 'Almacen Madre de Dios', 'ALMACEN', 3, '170101', 'Jr. 2 de Mayo 321, Puerto Maldonado', NULL, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06', NULL),
(5, 'Oficina 1 Juliaca', 'OFICINA', 1, '210101', 'Jr. Nuñez 210, Juliaca', 1, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06', NULL),
(6, 'Oficina 2 Juliaca', 'OFICINA', 1, '210101', 'Av. Ferroviaria 450, Juliaca', 1, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06', NULL),
(7, 'Oficina Cusco Centro', 'OFICINA', 2, '080101', 'Portal Belen 115, Cusco', NULL, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06', NULL),
(8, 'Oficina 2 Juliaca Quilca', 'OFICINA', 1, '210102', 'Jr. Quilca 88, Juliaca', NULL, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06', NULL),
(9, 'Oficina Abancay', 'OFICINA', 4, '030101', 'Jr. Armas 456, Abancay', NULL, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06', NULL),
(10, 'Oficina Puerto Maldonado', 'OFICINA', 3, '170101', 'Jr. Prada 200, Puerto Maldonado', NULL, 1, '2026-05-04 20:42:06', '2026-05-04 20:42:06', NULL),
(11, 'Sucursal Central TyG', 'ALMACEN', 1, '150101', 'Av. Los Angeles 123', NULL, 1, '2026-05-07 03:11:32', '2026-05-07 03:11:32', NULL);

-- --------------------------------------------------------

--
-- Table structure for table `usuarios`
--

CREATE TABLE `usuarios` (
  `id` bigint NOT NULL,
  `username` varchar(100) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `nombre_completo` varchar(255) NOT NULL,
  `email` varchar(255) DEFAULT NULL,
  `rol` enum('ADMIN','JEFE_ALMACEN','ALMACENERO','VENDEDOR','CONTADOR') NOT NULL,
  `sucursal_id` bigint DEFAULT NULL COMMENT 'NULL para ADMIN y JEFE_ALMACEN que operan en multiples zonas',
  `zona_id` bigint DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL COMMENT 'Soft delete',
  `created_by_id` bigint DEFAULT NULL,
  `ultimo_login` timestamp NULL DEFAULT NULL COMMENT 'Actualizado en cada login exitoso. Útil para auditoría',
  `bloqueado_hasta` timestamp NULL DEFAULT NULL COMMENT 'NULL = cuenta libre. Si es futuro, Spring rechaza el login sin tocar la BD',
  `intentos_fallidos` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Usuarios del sistema. Rol CONTADOR permite solo descargar reportes sin modificar datos';

--
-- Dumping data for table `usuarios`
--

INSERT INTO `usuarios` (`id`, `username`, `password_hash`, `nombre_completo`, `email`, `rol`, `sucursal_id`, `zona_id`, `activo`, `created_at`, `updated_at`, `deleted_at`, `created_by_id`, `ultimo_login`, `bloqueado_hasta`, `intentos_fallidos`) VALUES
(1, 'admin.tyg', '$2a$12$s/JH3BWVONKitXd6A8Jx1e3vg3Ry9gYbYDdyDCNpvwMvrVrQuO7gO', 'Yhomar Torres Mamani', 'admin@tyg.pe', 'ADMIN', NULL, NULL, 1, '2026-05-04 20:42:06', '2026-06-12 20:56:25', NULL, NULL, '2026-06-13 01:56:25', NULL, 0),
(2, 'percy.lopez', '$2a$12$s/JH3BWVONKitXd6A8Jx1e3vg3Ry9gYbYDdyDCNpvwMvrVrQuO7gO', 'Percy Lopez Quispe', 'percy@tyg.pe', 'JEFE_ALMACEN', 1, NULL, 1, '2026-05-04 20:42:06', '2026-06-01 03:23:51', NULL, 1, '2026-06-01 08:23:52', NULL, 0),
(3, 'javier.sucari', '$2a$12$s/JH3BWVONKitXd6A8Jx1e3vg3Ry9gYbYDdyDCNpvwMvrVrQuO7gO', 'Javier Sucari Calisaya', 'javier@tyg.pe', 'ALMACENERO', 1, NULL, 1, '2026-05-04 20:42:06', '2026-05-31 02:02:42', NULL, 1, '2026-05-31 07:02:43', NULL, 0),
(4, 'nora.flores', '$2a$12$s/JH3BWVONKitXd6A8Jx1e3vg3Ry9gYbYDdyDCNpvwMvrVrQuO7gO', 'Nora Flores Alca', 'nora@tyg.pe', 'VENDEDOR', 9, 1, 1, '2026-05-04 20:42:06', '2026-05-31 02:02:23', NULL, 1, '2026-05-31 07:02:23', NULL, 0),
(5, 'keiko.quispe', '$2a$12$s/JH3BWVONKitXd6A8Jx1e3vg3Ry9gYbYDdyDCNpvwMvrVrQuO7gO', 'Keiko Quispe Mamani', 'keiko@tyg.pe', 'VENDEDOR', 6, NULL, 1, '2026-05-04 20:42:06', '2026-05-06 22:01:52', NULL, 1, NULL, NULL, 0),
(6, 'maricarmen.s', '$2a$12$s/JH3BWVONKitXd6A8Jx1e3vg3Ry9gYbYDdyDCNpvwMvrVrQuO7gO', 'Maricarmen Sanchez Paz', 'maricarmen@tyg.pe', 'VENDEDOR', 9, NULL, 1, '2026-05-04 20:42:06', '2026-05-06 22:01:54', NULL, 1, NULL, NULL, 0),
(7, 'brayan.colque', '$2a$12$s/JH3BWVONKitXd6A8Jx1e3vg3Ry9gYbYDdyDCNpvwMvrVrQuO7gO', 'Brayan Colque Turpo', 'brayan@tyg.pe', 'VENDEDOR', 5, NULL, 1, '2026-05-04 20:42:06', '2026-05-06 22:01:56', NULL, 1, NULL, NULL, 0),
(8, 'rosa.mamani', '$2a$12$s/JH3BWVONKitXd6A8Jx1e3vg3Ry9gYbYDdyDCNpvwMvrVrQuO7gO', 'Rosa Mamani Calcina', 'rosa@tyg.pe', 'VENDEDOR', 7, NULL, 1, '2026-05-04 20:42:06', '2026-05-06 22:01:59', NULL, 1, NULL, NULL, 0),
(9, 'contador.tyg', '$2a$12$s/JH3BWVONKitXd6A8Jx1e3vg3Ry9gYbYDdyDCNpvwMvrVrQuO7gO', 'Luis Apaza Contreras', 'contador@tyg.pe', 'CONTADOR', 11, 1, 1, '2026-05-04 20:42:06', '2026-06-01 03:26:15', NULL, 1, '2026-06-01 08:26:15', NULL, 0),
(10, 'freelance.rm', '$2a$12$s/JH3BWVONKitXd6A8Jx1e3vg3Ry9gYbYDdyDCNpvwMvrVrQuO7gO', 'Ricardo Mamani', 'rm.freelance@tyg.pe', 'VENDEDOR', NULL, 1, 1, '2026-05-04 20:42:06', '2026-05-06 22:36:21', NULL, 1, NULL, NULL, 0);
-- Se eliminó el registro huérfano (11, 'admin.nacional', ...): tenía rol VENDEDOR por error,
-- estaba soft-deleted (activo=0) y era un remanente de la confusión administrador(sistema) vs administrador(empresa).

-- --------------------------------------------------------

--
-- Table structure for table `ventas`
--

CREATE TABLE `ventas` (
  `id` bigint NOT NULL,
  `item_kit_id` bigint NOT NULL COMMENT 'UNIQUE garantiza que un kit fisico no se venda dos veces',
  `vendedor_id` bigint NOT NULL,
  `cliente_id` bigint NOT NULL,
  `sucursal_venta_id` bigint NOT NULL,
  `monto_venta` decimal(10,2) NOT NULL,
  `monto_liquidado` decimal(10,2) DEFAULT NULL COMMENT 'Se actualiza al cerrar la liquidacion del periodo',
  `condicion` enum('CONTADO','CREDITO') NOT NULL,
  `tipo_vendedor` enum('EMPLEADO') NOT NULL DEFAULT 'EMPLEADO',
  `metodo_pago` varchar(50) DEFAULT NULL COMMENT 'EFECTIVO | YAPE | PLIN | TRANSFERENCIA',
  `liquidacion_id` bigint DEFAULT NULL COMMENT 'Se asigna al cerrar la liquidacion del periodo',
  `fecha_venta` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `created_by_id` bigint NOT NULL COMMENT 'Quien registro la venta (puede diferir del vendedor)',
  `estado` enum('ACTIVA','ANULADA') NOT NULL DEFAULT 'ACTIVA',
  `motivo_anulacion` varchar(500) DEFAULT NULL,
  `anulada_por_id` bigint DEFAULT NULL,
  `fecha_anulacion` timestamp NULL DEFAULT NULL,
  `item_kit_activo` bigint GENERATED ALWAYS AS (if((`estado` = _utf8mb4'ACTIVA'),`item_kit_id`,NULL)) STORED
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Registro de venta al consumidor final. UNIQUE en item_kit_id es la primera linea de defensa contra doble venta';

--
-- Dumping data for table `ventas`
--

INSERT INTO `ventas` (`id`, `item_kit_id`, `vendedor_id`, `cliente_id`, `sucursal_venta_id`, `monto_venta`, `monto_liquidado`, `condicion`, `tipo_vendedor`, `metodo_pago`, `liquidacion_id`, `fecha_venta`, `created_by_id`, `estado`, `motivo_anulacion`, `anulada_por_id`, `fecha_anulacion`) VALUES
(1, 1, 6, 1, 9, '110.00', '110.00', 'CONTADO', 'EMPLEADO', 'EFECTIVO', 1, '2025-01-20 15:30:00', 6, 'ACTIVA', NULL, NULL, NULL),
(2, 5, 4, 2, 9, '140.00', '140.00', 'CONTADO', 'EMPLEADO', 'YAPE', 1, '2025-01-22 20:00:00', 4, 'ACTIVA', NULL, NULL, NULL),
(3, 6, 4, 3, 9, '110.00', NULL, 'CREDITO', 'EMPLEADO', 'TRANSFERENCIA', 2, '2025-01-25 16:00:00', 4, 'ACTIVA', NULL, NULL, NULL),
(4, 11, 8, 4, 7, '110.00', '110.00', 'CONTADO', 'EMPLEADO', 'EFECTIVO', 3, '2025-01-24 14:00:00', 8, 'ACTIVA', NULL, NULL, NULL),
(5, 13, 8, 5, 7, '140.00', '140.00', 'CONTADO', 'EMPLEADO', 'PLIN', 3, '2025-01-26 19:30:00', 8, 'ACTIVA', NULL, NULL, NULL),
(6, 25, 4, 6, 10, '80.00', '80.00', 'CONTADO', 'EMPLEADO', 'EFECTIVO', 7, '2026-05-02 21:00:00', 1, 'ACTIVA', NULL, NULL, NULL),
(7, 19, 5, 7, 6, '140.00', '140.00', 'CONTADO', 'EMPLEADO', 'YAPE', 4, '2025-02-10 15:00:00', 5, 'ACTIVA', NULL, NULL, NULL),
(8, 10, 6, 8, 9, '110.00', '110.00', 'CONTADO', 'EMPLEADO', 'EFECTIVO', 2, '2025-01-28 17:00:00', 6, 'ANULADA', 'KitDefectuoso', 1, '2026-05-13 06:21:57'),
(9, 24, 4, 15, 9, '110.00', '110.00', 'CONTADO', 'EMPLEADO', 'YAPE', 7, '2026-05-06 02:13:56', 1, 'ANULADA', 'KitDefectuoso', 1, '2026-05-13 02:25:38'),
(10, 8, 4, 20, 9, '120.00', NULL, 'CONTADO', 'EMPLEADO', 'EFECTIVO', NULL, '2026-05-31 06:36:45', 4, 'ACTIVA', NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Table structure for table `zonas`
--

CREATE TABLE `zonas` (
  `id` bigint NOT NULL,
  `codigo_zona` varchar(20) NOT NULL COMMENT 'Ej: Z122000 = Puno, Z122002 = Cusco',
  `nombre` varchar(100) NOT NULL,
  `region` varchar(100) DEFAULT NULL,
  `activo` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Zonas/departamentos segun codificacion DirecTV';

--
-- Dumping data for table `zonas`
--

INSERT INTO `zonas` (`id`, `codigo_zona`, `nombre`, `region`, `activo`, `created_at`) VALUES
(1, 'Z122000', 'Puno', 'Sur', 1, '2026-05-04 20:42:01'),
(2, 'Z122002', 'Cusco', 'Sur', 1, '2026-05-04 20:42:01'),
(3, 'Z122070', 'Madre de Dios', 'Sur', 1, '2026-05-04 20:42:01'),
(4, 'Z122080', 'Apurimac', 'Sur', 1, '2026-05-04 20:42:01'),
(5, 'Z122090', 'Lima', 'Lima', 0, '2026-05-07 03:39:07');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `activaciones`
--
ALTER TABLE `activaciones`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `venta_id` (`venta_id`),
  ADD KEY `registrado_por_id` (`registrado_por_id`);

--
-- Indexes for table `audit_log`
--
ALTER TABLE `audit_log`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_tabla_registro` (`tabla_nombre`,`registro_id`),
  ADD KEY `idx_usuario_fecha` (`usuario_id`,`fecha`),
  ADD KEY `idx_fecha` (`fecha`);

--
-- Indexes for table `clientes`
--
ALTER TABLE `clientes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `dni` (`dni`),
  ADD UNIQUE KEY `ruc` (`ruc`);

--
-- Indexes for table `despachos`
--
ALTER TABLE `despachos`
  ADD PRIMARY KEY (`id`),
  ADD KEY `sucursal_origen_id` (`sucursal_origen_id`),
  ADD KEY `sucursal_destino_id` (`sucursal_destino_id`),
  ADD KEY `usuario_envia_id` (`usuario_envia_id`),
  ADD KEY `usuario_recibe_id` (`usuario_recibe_id`);

--
-- Indexes for table `despacho_items`
--
ALTER TABLE `despacho_items`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_despacho_item` (`despacho_id`,`item_kit_id`) COMMENT 'Un kit no puede aparecer dos veces en el mismo despacho',
  ADD KEY `item_kit_id` (`item_kit_id`);

--
-- Indexes for table `historial_custodios`
--
ALTER TABLE `historial_custodios`
  ADD PRIMARY KEY (`id`),
  ADD KEY `sucursal_anterior_id` (`sucursal_anterior_id`),
  ADD KEY `sucursal_nueva_id` (`sucursal_nueva_id`),
  ADD KEY `custodio_anterior_id` (`custodio_anterior_id`),
  ADD KEY `custodio_nuevo_id` (`custodio_nuevo_id`),
  ADD KEY `registrado_por_id` (`registrado_por_id`),
  ADD KEY `idx_item_fecha` (`item_kit_id`,`fecha_evento`);

--
-- Indexes for table `items_kit`
--
ALTER TABLE `items_kit`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `serie_maestro` (`serie_maestro`),
  ADD UNIQUE KEY `serie_sim` (`serie_sim`),
  ADD UNIQUE KEY `serie_deco` (`serie_deco`),
  ADD UNIQUE KEY `serie_deco_2` (`serie_deco`),
  ADD KEY `lote_id` (`lote_id`),
  ADD KEY `producto_id` (`producto_id`),
  ADD KEY `sucursal_actual_id` (`sucursal_actual_id`),
  ADD KEY `custodio_actual_id` (`custodio_actual_id`),
  ADD KEY `idx_serie_maestro` (`serie_maestro`),
  ADD KEY `idx_serie_sim` (`serie_sim`),
  ADD KEY `idx_serie_deco` (`serie_deco`),
  ADD KEY `idx_estado_sucursal` (`estado`,`sucursal_actual_id`),
  ADD KEY `modelo_kit_id` (`modelo_kit_id`);

--
-- Indexes for table `kardex_mensual`
--
ALTER TABLE `kardex_mensual`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `uq_kardex_periodo` (`sucursal_id`,`producto_id`,`periodo`),
  ADD KEY `producto_id` (`producto_id`),
  ADD KEY `generado_por_id` (`generado_por_id`);

--
-- Indexes for table `liquidaciones_caja`
--
ALTER TABLE `liquidaciones_caja`
  ADD PRIMARY KEY (`id`),
  ADD KEY `vendedor_id` (`vendedor_id`),
  ADD KEY `sucursal_id` (`sucursal_id`),
  ADD KEY `aprobado_por_id` (`aprobado_por_id`);

--
-- Indexes for table `lotes`
--
ALTER TABLE `lotes`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `numero_pedido` (`numero_pedido`),
  ADD KEY `zona_id` (`zona_id`),
  ADD KEY `sucursal_recepcion_id` (`sucursal_recepcion_id`),
  ADD KEY `usuario_registro_id` (`usuario_registro_id`);

--
-- Indexes for table `modelos_kit`
--
ALTER TABLE `modelos_kit`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codigo` (`codigo`);

--
-- Indexes for table `productos`
--
ALTER TABLE `productos`
  ADD PRIMARY KEY (`id`);

--
-- Indexes for table `sucursales`
--
ALTER TABLE `sucursales`
  ADD PRIMARY KEY (`id`),
  ADD KEY `zona_id` (`zona_id`),
  ADD KEY `ubicacion_fisica_id` (`ubicacion_fisica_id`);

--
-- Indexes for table `usuarios`
--
ALTER TABLE `usuarios`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `sucursal_id` (`sucursal_id`),
  ADD KEY `created_by_id` (`created_by_id`),
  ADD KEY `fk_usuarios_zona` (`zona_id`);

--
-- Indexes for table `ventas`
--
ALTER TABLE `ventas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `item_kit_id` (`item_kit_id`),
  ADD UNIQUE KEY `uq_kit_venta_activa` (`item_kit_activo`),
  ADD KEY `cliente_id` (`cliente_id`),
  ADD KEY `liquidacion_id` (`liquidacion_id`),
  ADD KEY `created_by_id` (`created_by_id`),
  ADD KEY `idx_vendedor_fecha` (`vendedor_id`,`fecha_venta`),
  ADD KEY `idx_sucursal_fecha` (`sucursal_venta_id`,`fecha_venta`);

--
-- Indexes for table `zonas`
--
ALTER TABLE `zonas`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `codigo_zona` (`codigo_zona`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `activaciones`
--
ALTER TABLE `activaciones`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `audit_log`
--
ALTER TABLE `audit_log`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=36;

--
-- AUTO_INCREMENT for table `clientes`
--
ALTER TABLE `clientes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=21;

--
-- AUTO_INCREMENT for table `despachos`
--
ALTER TABLE `despachos`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=29;

--
-- AUTO_INCREMENT for table `despacho_items`
--
ALTER TABLE `despacho_items`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=26;

--
-- AUTO_INCREMENT for table `historial_custodios`
--
ALTER TABLE `historial_custodios`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=43;

--
-- AUTO_INCREMENT for table `items_kit`
--
ALTER TABLE `items_kit`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=36;

--
-- AUTO_INCREMENT for table `kardex_mensual`
--
ALTER TABLE `kardex_mensual`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `liquidaciones_caja`
--
ALTER TABLE `liquidaciones_caja`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT for table `lotes`
--
ALTER TABLE `lotes`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `modelos_kit`
--
ALTER TABLE `modelos_kit`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `productos`
--
ALTER TABLE `productos`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `sucursales`
--
ALTER TABLE `sucursales`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `usuarios`
--
ALTER TABLE `usuarios`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=12;

--
-- AUTO_INCREMENT for table `ventas`
--
ALTER TABLE `ventas`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `zonas`
--
ALTER TABLE `zonas`
  MODIFY `id` bigint NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `activaciones`
--
ALTER TABLE `activaciones`
  ADD CONSTRAINT `activaciones_ibfk_1` FOREIGN KEY (`venta_id`) REFERENCES `ventas` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `activaciones_ibfk_2` FOREIGN KEY (`registrado_por_id`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `despachos`
--
ALTER TABLE `despachos`
  ADD CONSTRAINT `despachos_ibfk_1` FOREIGN KEY (`sucursal_origen_id`) REFERENCES `sucursales` (`id`),
  ADD CONSTRAINT `despachos_ibfk_2` FOREIGN KEY (`sucursal_destino_id`) REFERENCES `sucursales` (`id`),
  ADD CONSTRAINT `despachos_ibfk_3` FOREIGN KEY (`usuario_envia_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `despachos_ibfk_4` FOREIGN KEY (`usuario_recibe_id`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `despacho_items`
--
ALTER TABLE `despacho_items`
  ADD CONSTRAINT `despacho_items_ibfk_1` FOREIGN KEY (`despacho_id`) REFERENCES `despachos` (`id`),
  ADD CONSTRAINT `despacho_items_ibfk_2` FOREIGN KEY (`item_kit_id`) REFERENCES `items_kit` (`id`);

--
-- Constraints for table `historial_custodios`
--
ALTER TABLE `historial_custodios`
  ADD CONSTRAINT `historial_custodios_ibfk_1` FOREIGN KEY (`item_kit_id`) REFERENCES `items_kit` (`id`),
  ADD CONSTRAINT `historial_custodios_ibfk_2` FOREIGN KEY (`sucursal_anterior_id`) REFERENCES `sucursales` (`id`),
  ADD CONSTRAINT `historial_custodios_ibfk_3` FOREIGN KEY (`sucursal_nueva_id`) REFERENCES `sucursales` (`id`),
  ADD CONSTRAINT `historial_custodios_ibfk_4` FOREIGN KEY (`custodio_anterior_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `historial_custodios_ibfk_5` FOREIGN KEY (`custodio_nuevo_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `historial_custodios_ibfk_6` FOREIGN KEY (`registrado_por_id`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `items_kit`
--
ALTER TABLE `items_kit`
  ADD CONSTRAINT `items_kit_ibfk_1` FOREIGN KEY (`lote_id`) REFERENCES `lotes` (`id`),
  ADD CONSTRAINT `items_kit_ibfk_2` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  ADD CONSTRAINT `items_kit_ibfk_3` FOREIGN KEY (`sucursal_actual_id`) REFERENCES `sucursales` (`id`),
  ADD CONSTRAINT `items_kit_ibfk_4` FOREIGN KEY (`custodio_actual_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `items_kit_ibfk_5` FOREIGN KEY (`modelo_kit_id`) REFERENCES `modelos_kit` (`id`);

--
-- Constraints for table `kardex_mensual`
--
ALTER TABLE `kardex_mensual`
  ADD CONSTRAINT `kardex_mensual_ibfk_1` FOREIGN KEY (`sucursal_id`) REFERENCES `sucursales` (`id`),
  ADD CONSTRAINT `kardex_mensual_ibfk_2` FOREIGN KEY (`producto_id`) REFERENCES `productos` (`id`),
  ADD CONSTRAINT `kardex_mensual_ibfk_3` FOREIGN KEY (`generado_por_id`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `liquidaciones_caja`
--
ALTER TABLE `liquidaciones_caja`
  ADD CONSTRAINT `liquidaciones_caja_ibfk_1` FOREIGN KEY (`vendedor_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `liquidaciones_caja_ibfk_2` FOREIGN KEY (`sucursal_id`) REFERENCES `sucursales` (`id`),
  ADD CONSTRAINT `liquidaciones_caja_ibfk_3` FOREIGN KEY (`aprobado_por_id`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `lotes`
--
ALTER TABLE `lotes`
  ADD CONSTRAINT `lotes_ibfk_1` FOREIGN KEY (`zona_id`) REFERENCES `zonas` (`id`),
  ADD CONSTRAINT `lotes_ibfk_2` FOREIGN KEY (`sucursal_recepcion_id`) REFERENCES `sucursales` (`id`),
  ADD CONSTRAINT `lotes_ibfk_3` FOREIGN KEY (`usuario_registro_id`) REFERENCES `usuarios` (`id`);

--
-- Constraints for table `sucursales`
--
ALTER TABLE `sucursales`
  ADD CONSTRAINT `sucursales_ibfk_1` FOREIGN KEY (`zona_id`) REFERENCES `zonas` (`id`),
  ADD CONSTRAINT `sucursales_ibfk_2` FOREIGN KEY (`ubicacion_fisica_id`) REFERENCES `sucursales` (`id`);

--
-- Constraints for table `usuarios`
--
ALTER TABLE `usuarios`
  ADD CONSTRAINT `fk_usuarios_zona` FOREIGN KEY (`zona_id`) REFERENCES `zonas` (`id`),
  ADD CONSTRAINT `usuarios_ibfk_1` FOREIGN KEY (`sucursal_id`) REFERENCES `sucursales` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `usuarios_ibfk_2` FOREIGN KEY (`created_by_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `usuarios_ibfk_3` FOREIGN KEY (`zona_id`) REFERENCES `zonas` (`id`);

--
-- Constraints for table `ventas`
--
ALTER TABLE `ventas`
  ADD CONSTRAINT `ventas_ibfk_1` FOREIGN KEY (`item_kit_id`) REFERENCES `items_kit` (`id`),
  ADD CONSTRAINT `ventas_ibfk_2` FOREIGN KEY (`vendedor_id`) REFERENCES `usuarios` (`id`),
  ADD CONSTRAINT `ventas_ibfk_3` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  ADD CONSTRAINT `ventas_ibfk_4` FOREIGN KEY (`sucursal_venta_id`) REFERENCES `sucursales` (`id`),
  ADD CONSTRAINT `ventas_ibfk_5` FOREIGN KEY (`liquidacion_id`) REFERENCES `liquidaciones_caja` (`id`) ON DELETE SET NULL,
  ADD CONSTRAINT `ventas_ibfk_6` FOREIGN KEY (`created_by_id`) REFERENCES `usuarios` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
