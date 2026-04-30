import facultyLogo from '../../assets/branding/logo-fqf.png';
import laboratoryLogo from '../../assets/branding/logo-tecnologia-farmaceutica.jpg';

const sizeMap = {
  mini: {
    wrapper: 'gap-3',
    grid: 'gap-2',
    facultyBox: 'h-11 w-11 rounded-2xl p-1.5',
    laboratoryBox: 'h-11 w-[4.4rem] rounded-2xl p-1.5',
    title: 'text-sm',
    subtitle: 'text-[0.62rem] tracking-[0.22em]',
  },
  compact: {
    wrapper: 'gap-3.5',
    grid: 'gap-2.5',
    facultyBox: 'h-14 w-14 rounded-[1.15rem] p-2',
    laboratoryBox: 'h-14 w-[5.5rem] rounded-[1.15rem] p-2',
    title: 'text-[0.95rem]',
    subtitle: 'text-[0.62rem] tracking-[0.24em]',
  },
  hero: {
    wrapper: 'gap-5',
    grid: 'gap-3',
    facultyBox: 'h-24 w-24 rounded-[1.75rem] p-3',
    laboratoryBox: 'h-24 w-[9.5rem] rounded-[1.75rem] p-3',
    title: 'text-xl sm:text-2xl',
    subtitle: 'text-[0.68rem] tracking-[0.3em]',
  },
};

function LogoTile({ src, alt, className }) {
  return (
    <div
      className={`overflow-hidden border border-white/70 bg-white/[0.96] shadow-[0_16px_32px_rgba(23,61,44,0.12)] backdrop-blur ${className}`}
    >
      <img src={src} alt={alt} className="h-full w-full object-contain" />
    </div>
  );
}

function InstitutionalBrand({
  size = 'compact',
  theme = 'default',
  showText = true,
  className = '',
}) {
  const config = sizeMap[size] ?? sizeMap.compact;
  const titleClassName = theme === 'inverse' ? 'text-white' : 'text-brand-ink';
  const subtitleClassName = theme === 'inverse' ? 'text-white/72' : 'text-copy-soft';
  const detailClassName = theme === 'inverse' ? 'text-white/78' : 'text-copy';

  return (
    <div className={`flex items-center ${config.wrapper} ${className}`}>
      <div className={`grid shrink-0 grid-cols-2 ${config.grid}`}>
        <LogoTile
          src={facultyLogo}
          alt="Logo de la Facultad de Quimica y Farmacia de la Universidad de El Salvador"
          className={config.facultyBox}
        />
        <LogoTile
          src={laboratoryLogo}
          alt="Logo de Tecnologia Farmaceutica de la Facultad de Quimica y Farmacia"
          className={config.laboratoryBox}
        />
      </div>

      {showText ? (
        <div className="min-w-0">
          <p className={`font-extrabold tracking-tight ${config.title} ${titleClassName}`}>
            Sistema de Inventario Q&amp;F
          </p>
          <p className={`mt-1 font-bold uppercase ${config.subtitle} ${subtitleClassName}`}>
            Facultad de Quimica y Farmacia
          </p>
          <p className={`text-sm font-semibold ${detailClassName}`}>Tecnologia Farmaceutica</p>
        </div>
      ) : null}
    </div>
  );
}

export default InstitutionalBrand;
