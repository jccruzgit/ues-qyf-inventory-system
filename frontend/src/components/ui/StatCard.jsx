import Badge from './Badge';
import Card from './Card';

function StatCard({
  title,
  value,
  meta,
  metaVariant = 'neutral',
  icon: Icon,
  iconClassName = '',
  accent = 'bg-brand-teal-soft text-brand-teal',
  illustration,
}) {
  return (
    <Card className="relative overflow-hidden p-5 sm:p-6">
      <div className="relative z-10 flex items-start justify-between gap-4">
        <div className={`inline-flex h-12 w-12 items-center justify-center rounded-2xl ${accent}`}>
          <Icon className={`h-5 w-5 ${iconClassName}`} strokeWidth={2.1} />
        </div>
        {meta ? <Badge variant={metaVariant}>{meta}</Badge> : null}
      </div>

      <div className="relative z-10 mt-10">
        <p className="text-sm font-bold text-copy">{title}</p>
        <p className="mt-2 text-[2rem] font-extrabold tracking-[-0.04em] text-brand-ink">
          {value}
        </p>
      </div>

      {illustration ? (
        <div className="pointer-events-none absolute bottom-3 right-3 text-brand-ink/[0.06]">
          {illustration}
        </div>
      ) : null}
    </Card>
  );
}

export default StatCard;
