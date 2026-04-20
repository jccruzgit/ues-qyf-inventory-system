function SectionHeader({ title, subtitle, action }) {
  return (
    <div className="flex items-start justify-between gap-4">
      <div>
        <h2 className="text-[1.65rem] font-extrabold tracking-[-0.04em] text-brand-ink">
          {title}
        </h2>
        {subtitle ? (
          <p className="mt-1 text-sm leading-6 text-copy">{subtitle}</p>
        ) : null}
      </div>

      {action ? <div className="shrink-0">{action}</div> : null}
    </div>
  );
}

export default SectionHeader;
