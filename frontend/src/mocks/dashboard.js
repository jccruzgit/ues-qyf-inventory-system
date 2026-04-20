import {
  Beaker,
  FlaskConical,
  Leaf,
  ShieldCheck,
} from 'lucide-react';

export const kpiCards = [
  {
    title: 'Total de productos',
    value: '1,248',
    meta: '+12.5%',
    metaVariant: 'success',
    icon: Beaker,
    accent: 'bg-[#e9f3ff] text-brand-ink',
    illustration: FlaskConical,
  },
  {
    title: 'Stock bajo',
    value: '14',
    meta: 'Critico',
    metaVariant: 'danger',
    icon: ShieldCheck,
    accent: 'bg-[#fdebec] text-[#d53a43]',
    illustration: Leaf,
  },
  {
    title: 'Lotes por vencer',
    value: '08',
    meta: 'Proximos 30 dias',
    metaVariant: 'success',
    icon: FlaskConical,
    accent: 'bg-[#ebf8ef] text-[#65a96e]',
    illustration: Beaker,
  },
];

export const movementSeries = [
  { day: 'LUN', intake: 42, usage: 30 },
  { day: 'MAR', intake: 56, usage: 44 },
  { day: 'MIE', intake: 34, usage: 48 },
  { day: 'JUE', intake: 62, usage: 37 },
  { day: 'VIE', intake: 54, usage: 45 },
  { day: 'SAB', intake: 30, usage: 24 },
  { day: 'DOM', intake: 38, usage: 29 },
];

export const recentActivity = [
  {
    title: 'Lote #921 de hidroxido de sodio',
    detail: 'Agregado por Dr. Vance',
    time: 'Hace 2 h',
    tone: 'teal',
  },
  {
    title: 'Etanol 99% - Retiro',
    detail: 'Usado en laboratorio 4B',
    time: 'Hace 5 h',
    tone: 'success',
  },
  {
    title: 'Por vencer: acido sulfurico',
    detail: 'El lote vence en 48 horas',
    time: 'Hace 1 dia',
    tone: 'danger',
  },
  {
    title: 'Protocolo de seguridad actualizado',
    detail: 'Actualizacion del sistema',
    time: 'Hace 2 dias',
    tone: 'neutral',
  },
];

export const optimizationActions = [
  { label: 'Solicitar ahora', variant: 'primary' },
  { label: 'Descartar', variant: 'secondary' },
];

export const complianceCard = {
  title: 'Cumplimiento de seguridad',
  description:
    'El laboratorio mantiene actualmente un 98% de cumplimiento con los estandares internacionales de almacenamiento quimico.',
  cta: 'Ejecutar auditoria',
};
