import { FlaskConical } from 'lucide-react';

function BrandMark({ className = '', iconClassName = '', label = 'Q&F' }) {
  return (
    <div
      className={`relative flex h-12 w-12 items-center justify-center rounded-2xl bg-brand-ink text-white shadow-[0_18px_35px_rgba(14,47,103,0.28)] ${className}`}
    >
      <div className="absolute inset-0 rounded-2xl bg-[radial-gradient(circle_at_top,_rgba(255,255,255,0.32),_transparent_65%)]" />
      <FlaskConical className={`relative h-5 w-5 ${iconClassName}`} strokeWidth={2.3} />
      <span className="sr-only">{label}</span>
    </div>
  );
}

export default BrandMark;
