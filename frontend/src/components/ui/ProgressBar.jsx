const toneClasses = {
  success: 'bg-[#2d7a49]',
  danger: 'bg-[#d53a43]',
  warning: 'bg-[#d28a19]',
  info: 'bg-brand-teal',
};

function ProgressBar({ value, max, tone = 'info', className = '' }) {
  const ratio = max > 0 ? Math.min((value / max) * 100, 100) : 0;

  return (
    <div className={`h-2 rounded-full bg-surface-2 ${className}`}>
      <div
        className={`h-full rounded-full transition-all duration-300 ${toneClasses[tone]}`}
        style={{ width: `${ratio}%` }}
      />
    </div>
  );
}

export default ProgressBar;
